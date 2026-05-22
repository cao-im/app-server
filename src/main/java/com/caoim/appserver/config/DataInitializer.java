package com.caoim.appserver.config;

import com.caoim.appserver.dao.UserMapper;
import com.caoim.appserver.entity.AppUser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        AppUser admin = userMapper.selectByUsername("admin");
        if (admin == null) {
            AppUser user = new AppUser();
            user.setUsername("admin");
            user.setPassword("$2a$10$TfjvtpjrdOabAukwCNlioO01jNsvLg67jFVSrmgAihwRMS0BuQXjK");
            user.setNickname("管理员");
            user.setStatus(1);
            userMapper.insert(user);
        }
    }
}
