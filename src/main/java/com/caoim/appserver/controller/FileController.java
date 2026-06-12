package com.caoim.appserver.controller;

import com.caoim.appserver.common.Result;
import com.caoim.appserver.dto.FileUploadVO;
import com.caoim.appserver.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/api/file/upload")
    @Operation(summary = "单文件上传", description = "上传单个文件，支持图片、文档、音视频等格式")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(file));
    }

    @PostMapping("/api/file/uploads")
    @Operation(summary = "多文件上传", description = "批量上传多个文件")
    public Result<List<FileUploadVO>> uploads(@RequestParam("files") MultipartFile[] files) {
        return Result.success(fileService.uploadBatch(files));
    }
}
