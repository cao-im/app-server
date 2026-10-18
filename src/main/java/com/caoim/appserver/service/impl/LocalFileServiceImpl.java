package com.caoim.appserver.service.impl;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.config.FileUploadConfig;
import com.caoim.appserver.dto.FileUploadVO;
import com.caoim.appserver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalFileServiceImpl implements FileService {

    private final FileUploadConfig config;

    public LocalFileServiceImpl(FileUploadConfig config) {
        this.config = config;
    }

    @Override
    public FileUploadVO upload(MultipartFile file) {
        UploadObject object = UploadObject.prepare(file, config);
        String localFilePath = config.getLocalPath() + File.separator + object.relativePath.replace("/", File.separator);
        File destFile = new File(localFilePath).getAbsoluteFile();
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.error("创建上传目录失败: {}", parentDir.getAbsolutePath());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        try {
            file.transferTo(destFile.toPath());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        String urlPath = config.getPrefix() + "/" + object.relativePath;
        FileUploadVO vo = new FileUploadVO();
        vo.setFileName(object.originalFilename);
        vo.setNewFileName(object.storedName);
        vo.setSize(file.getSize());
        vo.setUrlPath(urlPath);
        vo.setUrl(config.getDomain() + urlPath);
        return vo;
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
}
