package com.demo.userservice.Model.dto.response;

import com.demo.userservice.Model.dto.request.RegisterRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateOtpResponse {
	private String message;
	private RegisterRequest registerRequest;

}
