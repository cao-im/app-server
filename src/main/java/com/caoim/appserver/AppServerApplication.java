package com.caoim.appserver;

import com.caoim.appserver.config.ImServerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.caoim.appserver.dao")
@EnableFeignClients(basePackages = "com.caoim.imcore.client")
@EnableConfigurationProperties(ImServerConfig.class)
public class AppServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
        System.out.println("====================================");
        System.out.println("  App Server 启动成功!");
        System.out.println("  Swagger文档: http://localhost:8080/swagger-ui.html");
        System.out.println("====================================");
    }
}
