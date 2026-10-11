package com.caoim.appserver.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImFeignConfig {

    @Bean
    public RequestInterceptor imInternalTokenInterceptor(
            @Value("${im.server.internal-token}") String internalToken) {
        return template -> template.header("X-Internal-Token", internalToken);
    }
}
