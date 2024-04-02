package com.demo.userservice.Model.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    public RegisterRequest(String email2, String username2, String name2) {
		this.email=email2;
		this.username=username2;
		this.name=name2;
	}
	private String username;
    private String name;
    private String password;
    private String email;
    private String otp;
    private String store;
}
