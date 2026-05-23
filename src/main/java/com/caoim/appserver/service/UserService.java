package com.caoim.appserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoim.appserver.common.BusinessException;
import com.caoim.appserver.common.ErrorCode;
import com.caoim.appserver.dao.UserMapper;
import com.caoim.appserver.dto.UserDTO;
import com.caoim.appserver.entity.AppUser;
import com.caoim.imcore.client.ImFeignClient;
import com.caoim.imcore.common.Result;
import com.caoim.imcore.dto.LoginDTO;
import com.caoim.imcore.dto.RegisterDTO;
import com.caoim.imcore.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, AppUser> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ImFeignClient imFeignClient;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> clientLogin(String username, String md5Password) {
        AppUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String storedHash = user.getPassword();
        if (!passwordEncoder.matches(md5Password, storedHash)) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", convertToDTO(user));

        try {
            LoginDTO imLoginDTO = new LoginDTO();
            imLoginDTO.setUsername(username);
            imLoginDTO.setPassword(md5Password);

            Result<Map<String, Object>> imResult = imFeignClient.loginUser(imLoginDTO);
            if (imResult != null && imResult.getCode() == 200 && imResult.getData() != null) {
                result.put("imToken", imResult.getData().get("token"));
                log.info("IM 登录成功: username={}", username);
            } else {
                log.error("IM 登录失败: username={}, result={}", username, imResult);
            }
        } catch (Exception e) {
            log.error("调用 IM 服务登录失败: username={}, error={}", username, e.getMessage(), e);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> clientRegister(String username, String md5Password, String nickname) {
        AppUser existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        RegisterDTO imRegisterDTO = new RegisterDTO();
        imRegisterDTO.setUsername(username);
        imRegisterDTO.setPassword(md5Password);
        imRegisterDTO.setNickname(nickname);

        Result<Map<String, Object>> imResult = imFeignClient.registerUser(imRegisterDTO);
        if (imResult == null || imResult.getCode() != 200 || imResult.getData() == null) {
            log.error("IM 用户创建失败: username={}, result={}", username, imResult);
            throw new BusinessException(ErrorCode.IM_SERVICE_ERROR.getCode(), "IM服务创建用户失败，请稍后重试");
        }

        User imUser = convertToUser(imResult.getData().get("user"));
        if (imUser == null || imUser.getId() == null) {
            log.error("IM 用户创建成功但返回数据异常: username={}, data={}", username, imResult.getData());
            throw new BusinessException(ErrorCode.IM_SERVICE_ERROR.getCode(), "IM服务返回数据异常");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(md5Password));
        user.setNickname(nickname != null ? nickname : username);
        user.setImUserId(imUser.getId());
        user.setStatus(1);
        save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("user", convertToDTO(user));
        result.put("imToken", imResult.getData().get("token"));
        log.info("用户注册成功并关联IM用户: username={}, appUserId={}, imUserId={}", username, user.getId(), imUser.getId());

        return result;
    }

    private UserDTO convertToDTO(AppUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        if (user.getCreateTime() != null) {
            dto.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    @SuppressWarnings("unchecked")
    private User convertToUser(Object userData) {
        if (userData == null) {
            return null;
        }
        if (userData instanceof User) {
            return (User) userData;
        }
        if (userData instanceof Map) {
            try {
                return objectMapper.convertValue(userData, User.class);
            } catch (Exception e) {
                log.error("转换IM用户数据失败: data={}", userData, e);
                return null;
            }
        }
        log.error("不支持的IM用户数据类型: type={}", userData.getClass().getName());
        return null;
    }

    public UserDTO getUserInfo(String username) {
        AppUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToDTO(user);
    }
}
