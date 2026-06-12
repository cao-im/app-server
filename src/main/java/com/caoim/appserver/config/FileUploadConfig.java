package com.caoim.appserver.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

    private String type = "local";
    private String localPath = System.getProperty("user.dir") + "/files";
    private String domain = "http://127.0.0.1:8081";
    private String maxSizeStr = "10MB";
    private long maxSize;
    private List<String> allowedExtensions = new ArrayList<>();
    private boolean datePath = true;
    private String prefix = "/profile";

    @PostConstruct
    public void init() {
        this.maxSize = parseSize(maxSizeStr);
    }

    private long parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) {
            return 10 * 1024 * 1024;
        }
        sizeStr = sizeStr.trim().toUpperCase();
        try {
            if (sizeStr.endsWith("KB")) {
                return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024;
            } else if (sizeStr.endsWith("MB")) {
                return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024 * 1024;
            } else if (sizeStr.endsWith("GB")) {
                return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024 * 1024 * 1024;
            } else {
                return Long.parseLong(sizeStr);
            }
        } catch (NumberFormatException e) {
            return 10 * 1024 * 1024;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMaxSizeStr() {
        return maxSizeStr;
    }

    public void setMaxSizeStr(String maxSizeStr) {
        this.maxSizeStr = maxSizeStr;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSizeStr) {
        this.maxSize = parseSize(maxSizeStr);
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public boolean isDatePath() {
        return datePath;
    }

    public void setDatePath(boolean datePath) {
        this.datePath = datePath;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler(getPrefix() + "/**")
                        .addResourceLocations("file:" + getLocalPath() + File.separator);
            }
        };
    }
}
