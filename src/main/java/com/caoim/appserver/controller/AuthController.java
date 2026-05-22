package com.caoim.appserver.controller;

import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.common.Result;
import com.caoim.appserver.dto.LoginDTO;
import com.caoim.appserver.dto.RegisterDTO;
import com.caoim.appserver.dto.UserDTO;
import com.caoim.appserver.security.JwtTokenUtil;
import com.caoim.appserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "C端认证", description = "供Flutter Demo等客户端使用的登录注册接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/api/client/login")
    @Operation(summary = "C端用户登录", description = "客户端登录获取IM Token（密码需先做MD5加密后传输）")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> userServiceResult = userService.clientLogin(loginDTO.getUsername(), loginDTO.getPassword());
        String token = jwtTokenUtil.generateToken(loginDTO.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userServiceResult.get("user"));
        if (userServiceResult.get("imToken") != null) {
            data.put("imToken", userServiceResult.get("imToken"));
        }
        return Result.success(data);
    }

    @PostMapping("/api/client/register")
    @Operation(summary = "C端用户注册", description = "新用户注册并获取IM Token（密码需先做MD5加密后传输）")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Map<String, Object> userServiceResult = userService.clientRegister(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getNickname()
        );
        String token = jwtTokenUtil.generateToken(registerDTO.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userServiceResult.get("user"));
        if (userServiceResult.get("imToken") != null) {
            data.put("imToken", userServiceResult.get("imToken"));
        }
        return Result.success(data);
    }

    @PostMapping("/api/client/refresh-token")
    @Operation(summary = "Token续期", description = "使用当前有效Token换取新的Token（Token即将过期时调用）")
    public Result<Map<String, Object>> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "缺少Authorization请求头");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "Authorization格式错误，需以'Bearer '开头（注意Bearer后有空格）");
        }
        String oldToken = authHeader.substring(7);

        if (!jwtTokenUtil.validateToken(oldToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "token无效或已过期");
        }

        String username = jwtTokenUtil.getUsernameFromToken(oldToken);
        String newToken = jwtTokenUtil.generateToken(username);

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        return Result.success(data);
    }

    @GetMapping("/api/user/info")
    @Operation(summary = "查询用户信息", description = "获取当前登录用户的详细信息（需要Token认证）")
    public Result<UserDTO> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO userDTO = userService.getUserInfo(username);
        return Result.success(userDTO);
    }
}
