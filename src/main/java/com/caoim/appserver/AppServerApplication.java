package com.caoim.appserver;

import com.caoim.appserver.config.ImServerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@MapperScan("com.caoim.appserver.dao")
@EnableFeignClients(basePackages = "com.caoim.imcore.client")
@EnableConfigurationProperties(ImServerConfig.class)
public class AppServerApplication {

    private static final Logger log = LoggerFactory.getLogger(AppServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        org.springframework.core.env.Environment env = event.getApplicationContext().getEnvironment();
        int port = env.getProperty("server.port", Integer.class, 8080);
        String contextPath = normalizeContextPath(env.getProperty("server.servlet.context-path", ""));

        log.info("");
        log.info("========================================");
        log.info("🚀 App Server (C端认证服务) 启动成功!");
        log.info("========================================");
        log.info("  服务端口: {}", port);
        log.info("  API地址: http://localhost:{}{}", port, contextPath);

        boolean swaggerEnabled = env.getProperty("springdoc.swagger-ui.enabled", Boolean.class, true);
        if (swaggerEnabled) {
            String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui/index.html");
            log.info("  Swagger文档: http://localhost:{}{}{}", port, contextPath, swaggerPath);
            log.info("  API文档(JSON): http://localhost:{}{}/api-docs", port, contextPath);
        }

        log.info("  IM服务地址: {}", env.getProperty("im.server.url", "未配置"));
        log.info("========================================");
        log.info("");
    }

    /**
     * 规范化 context-path，避免产生双斜杠
     * - "" → ""
     * - "/" → ""
     * - "/api" → "/api"
     */
    private static String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            return "";
        }
        return contextPath;
    }
}
