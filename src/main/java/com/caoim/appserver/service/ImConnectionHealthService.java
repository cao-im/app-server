package com.caoim.appserver.service;

import com.caoim.imcore.client.ImFeignClient;
import com.caoim.imcore.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImConnectionHealthService {

    private final ImFeignClient imFeignClient;

    @Value("${im.server.url}")
    private String imServerUrl;

    public Map<String, Object> checkImServerConnection() {
        Map<String, Object> healthReport = new HashMap<>();
        healthReport.put("checkTime", LocalDateTime.now().toString());
        healthReport.put("targetUrl", imServerUrl);

        boolean isHealthy = false;
        String status = "DOWN";
        String message = "IM 服务不可用";

        try {
            log.info("正在检查 IM 服务健康状态: {}", imServerUrl);

            Result<String> pingResult = imFeignClient.ping();
            if (pingResult != null && "pong".equals(pingResult.getData())) {
                isHealthy = true;
                status = "UP";
                message = "IM 服务运行正常";

                Result<Map<String, Object>> healthInfo = imFeignClient.healthCheck();
                if (healthInfo != null && healthInfo.getData() != null) {
                    healthReport.put("serviceInfo", healthInfo.getData());
                }
            }

            log.info("IM 服务健康检查结果: {} - {}", status, message);

        } catch (Exception e) {
            status = "ERROR";
            message = "连接 IM 服务失败: " + e.getMessage();
            log.error("IM 服务健康检查异常: {}", e.getMessage(), e);
        }

        healthReport.put("status", status);
        healthReport.put("isHealthy", isHealthy);
        healthReport.put("message", message);

        return healthReport;
    }

    public boolean isImServerAvailable() {
        try {
            Result<String> pingResult = imFeignClient.ping();
            return pingResult != null && "pong".equals(pingResult.getData());
        } catch (Exception e) {
            log.warn("IM 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getImServerPortInfo() {
        Map<String, Object> portInfoReport = new HashMap<>();
        portInfoReport.put("checkTime", LocalDateTime.now().toString());

        try {
            Result<Map<String, Object>> portInfoResult = imFeignClient.getPortInfo();
            if (portInfoResult != null && portInfoResult.getData() != null) {
                portInfoReport.putAll(portInfoResult.getData());
                portInfoReport.put("status", "SUCCESS");
            } else {
                portInfoReport.put("status", "FAILED");
                portInfoReport.put("message", "无法获取端口信息");
            }
        } catch (Exception e) {
            portInfoReport.put("status", "ERROR");
            portInfoReport.put("message", "获取端口信息异常: " + e.getMessage());
            log.error("获取 IM 服务端口信息失败: {}", e.getMessage(), e);
        }

        return portInfoReport;
    }
}
