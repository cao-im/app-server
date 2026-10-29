package com.caoim.appserver.service.impl;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.config.FileUploadConfig;
import com.caoim.appserver.dto.FileUploadVO;
import com.caoim.appserver.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
        vo.setUrl(publicBase() + urlPath);
        return vo;
    }

    /**
     * 配置里的 domain 若是 127.0.0.1 / localhost，手机和其他设备打不开。
     * 这时改用本次上传请求的 Host（含局域网 IP），显式配置公网域名时仍以配置为准。
     */
    private String publicBase() {
        String configured = trimSlash(config.getDomain());
        if (!isLoopback(configured)) {
            return configured;
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            String host = firstHeader(request, "X-Forwarded-Host");
            if (host == null || host.isBlank()) {
                host = request.getHeader("Host");
            }
            if (host != null && !host.isBlank()) {
                String proto = firstHeader(request, "X-Forwarded-Proto");
                if (proto == null || proto.isBlank()) {
                    proto = request.getScheme();
                }
                return proto + "://" + host.trim();
            }
        }
        return configured;
    }

    private static boolean isLoopback(String base) {
        if (base == null || base.isBlank()) {
            return true;
        }
        try {
            String host = URI.create(base).getHost();
            return host == null
                    || "127.0.0.1".equals(host)
                    || "localhost".equalsIgnoreCase(host)
                    || "::1".equals(host);
        } catch (Exception e) {
            return false;
        }
    }

    private static String firstHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null) {
            return null;
        }
        int comma = value.indexOf(',');
        return (comma >= 0 ? value.substring(0, comma) : value).trim();
    }

    private static String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
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
