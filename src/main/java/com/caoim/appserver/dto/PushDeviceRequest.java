package com.caoim.appserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushDeviceRequest {

    @NotBlank
    private String platform;

    @NotBlank
    private String provider;

    @NotBlank
    private String token;

    @NotBlank
    private String deviceId;
}
