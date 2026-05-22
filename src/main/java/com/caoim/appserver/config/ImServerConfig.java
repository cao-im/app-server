package com.caoim.appserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;

@Slf4j
@Validated
@Configuration
@ConfigurationProperties(prefix = "im.server")
public class ImServerConfig {

    @NotBlank(message = "IM 服务地址配置 [im.server.url] 不能为空！请在 application.yml 中配置")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @PostConstruct
    public void validateAndLog() {
        log.info("====================================");
        log.info("  IM 服务连接配置");
        log.info("  目标地址: {}", url);
        log.info("====================================");
    }
}
