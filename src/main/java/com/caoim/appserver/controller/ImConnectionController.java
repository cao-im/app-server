package com.caoim.appserver.controller;

import com.caoim.appserver.service.ImConnectionHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "IM 服务连接管理")
@RestController
@RequestMapping("/api/im/connection")
@RequiredArgsConstructor
public class ImConnectionController {

    private final ImConnectionHealthService imConnectionHealthService;

    @Operation(summary = "检查 IM 服务连接状态")
    @GetMapping("/health")
    public Map<String, Object> checkImServerHealth() {
        return imConnectionHealthService.checkImServerConnection();
    }

    @Operation(summary = "快速检测 IM 服务是否可用")
    @GetMapping("/available")
    public Map<String, Object> checkAvailability() {
        Map<String, Object> result = new java.util.HashMap<>();
        boolean available = imConnectionHealthService.isImServerAvailable();
        result.put("available", available);
        result.put("status", available ? "UP" : "DOWN");
        result.put("checkTime", LocalDateTime.now().toString());
        return result;
    }

    @Operation(summary = "获取 IM 服务端口信息")
    @GetMapping("/port-info")
    public Map<String, Object> getPortInfo() {
        return imConnectionHealthService.getImServerPortInfo();
    }
}
