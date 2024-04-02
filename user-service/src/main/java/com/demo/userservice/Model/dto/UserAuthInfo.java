package com.demo.userservice.Model.dto;

import com.demo.userservice.Model.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthInfo {
    String username;
    Role role;
}
