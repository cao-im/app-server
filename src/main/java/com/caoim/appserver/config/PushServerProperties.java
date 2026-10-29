package com.caoim.appserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "push.server")
public class PushServerProperties {

    /** 未启动 push-server 时保持 false，登记接口直接返回未登记 */
    private boolean enabled = false;

    private String url = "http://127.0.0.1:8083";

    private String internalToken = "";
}
