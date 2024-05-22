package com.demo.userservice.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		
		final Map<String, Object> map = new HashMap<>();
		map.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		map.put("error", "Unauthorized");
		map.put("message", authException.getMessage());
		map.put("path", request.getServletPath());
		
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getOutputStream(), map);
	}

}
