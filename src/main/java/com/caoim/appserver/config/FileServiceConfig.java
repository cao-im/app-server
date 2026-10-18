package com.caoim.appserver.config;

import com.caoim.appserver.service.FileService;
import com.caoim.appserver.service.impl.LocalFileServiceImpl;
import com.caoim.appserver.service.impl.OssFileServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class FileServiceConfig {

    @Bean
    public FileService fileService(FileUploadConfig config) {
        if (!config.isObjectStorage()) {
            return new LocalFileServiceImpl(config);
        }
        FileUploadConfig.Oss oss = config.getOss();
        if (isBlank(oss.getEndpoint()) || isBlank(oss.getBucket())
                || isBlank(oss.getAccessKey()) || isBlank(oss.getSecretKey())) {
            throw new IllegalStateException("file.type 为对象存储时，需要配置 file.oss.endpoint、bucket、access-key、secret-key");
        }
        String region = isBlank(oss.getRegion()) ? "us-east-1" : oss.getRegion().trim();
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create(oss.getEndpoint().trim()))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(oss.getAccessKey().trim(), oss.getSecretKey())))
                .forcePathStyle(oss.isPathStyle())
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
        return new OssFileServiceImpl(config, client);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
