package com.caoim.appserver.dto;

import lombok.Data;

@Data
public class FileUploadVO {

    private String url;
    private String fileName;
    private String newFileName;
    private String urlPath;
    private Long size;
}
