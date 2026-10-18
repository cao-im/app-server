package com.caoim.appserver.service.impl;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.config.FileUploadConfig;
import com.caoim.appserver.dto.FileUploadVO;
import com.caoim.appserver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OssFileServiceImpl implements FileService, DisposableBean {

    private final FileUploadConfig config;
    private final S3Client client;

    public OssFileServiceImpl(FileUploadConfig config, S3Client client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public FileUploadVO upload(MultipartFile file) {
        UploadObject object = UploadObject.prepare(file, config);
        String bucket = config.getOss().getBucket();
        try {
            put(file, object, bucket, config.getOss().isPublicRead());
        } catch (S3Exception e) {
            if (!config.getOss().isPublicRead() || !aclRejected(e)) {
                log.error("对象存储上传失败: {}", e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorMessage());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
            log.warn("对象存储不接受公开读权限，改为按存储桶策略访问: {}", e.awsErrorDetails() == null ? e.getMessage() : e.awsErrorDetails().errorCode());
            try {
                put(file, object, bucket, false);
            } catch (AwsServiceException | SdkClientException | IOException retry) {
                log.error("对象存储上传失败", retry);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        } catch (AwsServiceException | SdkClientException | IOException e) {
            log.error("对象存储上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setFileName(object.originalFilename);
        vo.setNewFileName(object.storedName);
        vo.setSize(file.getSize());
        vo.setUrlPath(object.relativePath);
        vo.setUrl(config.getOss().resolvedPublicBase() + "/" + object.relativePath);
        return vo;
    }

    private void put(MultipartFile file, UploadObject object, String bucket, boolean publicRead) throws IOException {
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(object.relativePath)
                .contentType(object.contentType())
                .contentLength(file.getSize());
        if (publicRead) {
            request.acl(ObjectCannedACL.PUBLIC_READ);
        }
        try (InputStream input = file.getInputStream()) {
            client.putObject(request.build(), RequestBody.fromInputStream(input, file.getSize()));
        }
    }

    private boolean aclRejected(S3Exception e) {
        if (e.statusCode() == 501) {
            return true;
        }
        if (e.awsErrorDetails() == null || e.awsErrorDetails().errorCode() == null) {
            return false;
        }
        String code = e.awsErrorDetails().errorCode();
        return "AccessControlListNotSupported".equals(code) || "NotImplemented".equals(code);
    }

    @Override
    public List<FileUploadVO> uploadBatch(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        List<FileUploadVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(upload(file));
        }
        return result;
    }

    @Override
    public void destroy() {
        client.close();
    }
}
