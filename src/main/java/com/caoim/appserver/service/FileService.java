package com.caoim.appserver.service;

import com.caoim.appserver.dto.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    /**
     * 单文件上传
     * @param file 上传的文件
     * @return 文件上传结果VO
     */
    FileUploadVO upload(MultipartFile file);

    /**
     * 多文件批量上传
     * @param files 上传的文件数组
     * @return 文件上传结果列表
     */
    List<FileUploadVO> uploadBatch(MultipartFile[] files);
}
