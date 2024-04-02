package com.demo.userservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.userservice.Model.Entity.OtpDto;

public interface OtpRepo extends JpaRepository<OtpDto, Integer> {
 
	public OtpDto findByEmail(String email);
}
