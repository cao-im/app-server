package com.caoim.appserver.controller;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.common.Result;
import com.caoim.appserver.config.PushServerProperties;
import com.caoim.appserver.dto.PushDeviceRequest;
import com.caoim.appserver.dto.UserDTO;
import com.caoim.appserver.security.JwtTokenUtil;
import com.caoim.appserver.service.PushClient;
import com.caoim.appserver.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PushDeviceController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final PushServerProperties pushServerProperties;
    private final PushClient pushClient;

    public PushDeviceController(JwtTokenUtil jwtTokenUtil,
                                UserService userService,
                                PushServerProperties pushServerProperties,
                                PushClient pushClient) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.pushServerProperties = pushServerProperties;
        this.pushClient = pushClient;
    }

    @PostMapping("/api/client/push/device")
    public Result<Map<String, Object>> register(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                 @Valid @RequestBody PushDeviceRequest request) {
        UserDTO user = currentUser(authHeader);
        if (user.getImUserId() == null) {
            throw new BusinessException(ErrorCode.IM_SERVICE_ERROR.getCode(), "账号尚未关联 IM 用户");
        }
        if (!pushServerProperties.isEnabled()) {
            return Result.success(Map.of("registered", false));
        }
        pushClient.register(user.getImUserId(), request);
        return Result.success(Map.of("registered", true));
    }

    @DeleteMapping("/api/client/push/device")
    public Result<Map<String, Object>> disable(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestParam String deviceId) {
        UserDTO user = currentUser(authHeader);
        if (!pushServerProperties.isEnabled() || user.getImUserId() == null) {
            return Result.success(Map.of("registered", false));
        }
        pushClient.disable(user.getImUserId(), deviceId);
        return Result.success(Map.of("registered", false));
    }

    private UserDTO currentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7).trim();
        if (!jwtTokenUtil.validateToken(token) || jwtTokenUtil.isRefreshToken(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        return userService.getUserInfo(jwtTokenUtil.getUsernameFromToken(token));
    }
}
