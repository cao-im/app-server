package com.caoim.appserver.service;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.config.PushServerProperties;
import com.caoim.appserver.dto.PushDeviceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PushClient {

    private final PushServerProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public PushClient(PushServerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void register(Long imUserId, PushDeviceRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", imUserId);
        payload.put("platform", request.getPlatform());
        payload.put("provider", request.getProvider());
        payload.put("token", request.getToken());
        payload.put("deviceId", request.getDeviceId());
        exchange("POST", "/internal/device", payload);
    }

    public void disable(Long imUserId, String deviceId) {
        exchange("DELETE", "/internal/device?userId=" + imUserId + "&deviceId=" + urlEncode(deviceId), null);
    }

    private void exchange(String method, String path, Map<String, Object> payload) {
        try {
            String base = properties.getUrl() == null ? "" : properties.getUrl().trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(base + path))
                    .timeout(Duration.ofSeconds(3))
                    .header("X-Internal-Token", properties.getInternalToken() == null ? "" : properties.getInternalToken());
            if ("DELETE".equals(method)) {
                builder.DELETE();
            } else {
                builder.header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300 || (response.body() != null && response.body().contains("\"code\":403"))) {
                throw new BusinessException(500, "推送服务拒绝登记");
            }
            if (response.body() != null && response.body().contains("\"code\":400")) {
                throw new BusinessException(400, "推送设备参数不正确");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "推送服务不可用");
        }
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
