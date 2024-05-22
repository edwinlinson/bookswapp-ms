package com.demo.userservice.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {
	
	@Autowired
	private UserService service;
	
	@GetMapping("/profile")
	public User profile() {
		System.out.println("Inside profile get");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		User user = service.findByEmail(email);
		System.out.println(" User info is "+user+" email "+email);
		return user;
	}
}
