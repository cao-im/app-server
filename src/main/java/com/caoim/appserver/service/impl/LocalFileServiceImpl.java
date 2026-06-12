package com.caoim.appserver.service.impl;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.config.FileUploadConfig;
import com.caoim.appserver.dto.FileUploadVO;
import com.caoim.appserver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("localFileService")
public class LocalFileServiceImpl implements FileService {

    private final FileUploadConfig config;

    @Autowired
    public LocalFileServiceImpl(FileUploadConfig config) {
        this.config = config;
    }

    @Override
    public FileUploadVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > config.getMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        String extension = "";
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        List<String> allowedExtensions = config.getAllowedExtensions();
        if (!allowedExtensions.isEmpty()) {
            if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
                throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
            }
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        String datePath = "";
        if (config.isDatePath()) {
            datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }

        StringBuilder localDirBuilder = new StringBuilder(config.getLocalPath());
        if (config.isDatePath()) {
            localDirBuilder.append(File.separator).append(datePath.replace("/", File.separator));
        }
        String localFilePath = localDirBuilder.append(File.separator).append(newFileName).toString();

        File destFile = new File(localFilePath);
        File parentDir = destFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setFileName(originalFilename);
        vo.setNewFileName(newFileName);
        vo.setSize(file.getSize());

        StringBuilder urlPathBuilder = new StringBuilder(config.getPrefix());
        if (config.isDatePath()) {
            urlPathBuilder.append("/").append(datePath);
        }
        urlPathBuilder.append("/").append(newFileName);
        String urlPath = urlPathBuilder.toString();

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
