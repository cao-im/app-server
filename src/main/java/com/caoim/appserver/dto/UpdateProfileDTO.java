package com.caoim.appserver.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
}
