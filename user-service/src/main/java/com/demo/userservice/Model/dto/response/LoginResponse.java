package com.demo.userservice.Model.dto.response;

import com.demo.userservice.Model.Entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String accessToken;
    private User user;
}
