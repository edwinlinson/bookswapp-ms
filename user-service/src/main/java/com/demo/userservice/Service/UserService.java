package com.demo.userservice.Service;

import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Model.dto.UserAuthInfo;
import com.demo.userservice.Model.dto.request.RegisterRequest;


import org.springframework.stereotype.Service;

@Service
public interface UserService {
    void registerUser(RegisterRequest registerRequest);
    void sendOtp(RegisterRequest email);
    boolean validateOtp(String email,Integer otp);
    boolean emailAlreadyUsed(String email);
    void registerOathUser(String email, String name ,String dp);
    UserAuthInfo getUserAuthInfoFromEmail(String email);
    String getUsernameFromEmail(String email);
    User findByUserName(String username);
    User findByEmail(String email);
}
