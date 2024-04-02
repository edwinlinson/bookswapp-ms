package com.demo.userservice.Model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OtpDto {
    @JsonProperty("email")
    private String email;
    @JsonProperty("otp")
    private String otp;
}
