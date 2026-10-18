package com.caoim.appserver.service.impl;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.config.FileUploadConfig;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

final class UploadObject {

    final String originalFilename;
    final String storedName;
    final String relativePath;
    final String extension;

    private UploadObject(String originalFilename, String storedName, String relativePath, String extension) {
        this.originalFilename = originalFilename;
        this.storedName = storedName;
        this.relativePath = relativePath;
        this.extension = extension;
    }

    static UploadObject prepare(MultipartFile file, FileUploadConfig config) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > config.getMaxSize()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        int slash = Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\'));
        if (slash >= 0 && slash < original.length() - 1) {
            original = original.substring(slash + 1);
        }
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0 && dot < original.length() - 1) {
            extension = original.substring(dot + 1).toLowerCase();
        }
        List<String> allowed = config.getAllowedExtensions();
        if (allowed != null && !allowed.isEmpty() && (extension.isEmpty() || !allowed.contains(extension))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        String stored = UUID.randomUUID().toString().replace("-", "");
        if (!extension.isEmpty()) {
            stored = stored + "." + extension;
        }
        String relative = stored;
        if (config.isDatePath()) {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            relative = date + "/" + stored;
        }
        return new UploadObject(original, stored, relative, extension);
    }

    String contentType() {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "txt", "md" -> "text/plain";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "m4a" -> "audio/mp4";
            case "aac" -> "audio/aac";
            case "amr" -> "audio/amr";
            case "ogg" -> "audio/ogg";
            case "opus" -> "audio/opus";
            case "mp4" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            default -> "application/octet-stream";
        };
    }
}
