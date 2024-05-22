package com.demo.userservice.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private UserService service;

	@GetMapping("/users")
	public String getUsers(){
//		List<User> users = service.getClass();
		return "users";
	}
}
