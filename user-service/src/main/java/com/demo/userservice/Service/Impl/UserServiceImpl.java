package com.demo.userservice.Service.Impl;

import com.demo.userservice.Exceptions.InvalidInputException;
import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Model.Enums.Role;
import com.demo.userservice.Model.dto.UserAuthInfo;
import com.demo.userservice.Model.dto.request.RegisterRequest;
import com.demo.userservice.Repositories.UserRepo;
import com.demo.userservice.Service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        validateUserRegisterDto(registerRequest);
        try{
            User user =User.builder().
                    username(registerRequest.getUsername())
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(userRepo.count()<1? Role.ROLE_ADMIN : Role.ROLE_USER)
                    .build();
            userRepo.save(user);
            System.out.println("user saved");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("user not saved");
            throw new RuntimeException("Something Went Wrong");
        }
    }

    @Override
    public void sendOtp(RegisterRequest email) {
        validateUserRegisterDto(email);
    }

    @Override
    public boolean validateOtp(String email, Integer otp) {
        return false;
    }

    @Override
    public boolean emailAlreadyUsed(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    public void registerOathUser(String email, String name, String dp) {
        User user = User.builder()
                .email(email)
                .username(name.replace(" ",""))
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.ROLE_USER)
                .dpUrl(dp).build();
        userRepo.save(user);
    }

    @Override
    public UserAuthInfo getUserAuthInfoFromEmail(String email) {
        User user = userRepo.findByEmail(email);
        return new UserAuthInfo(user.getUsername(),user.getRole());
    }

    @Override
    public String getUsernameFromEmail(String email) {
        return userRepo.findByEmail(email).getUsername();
    }

    private void validateUserRegisterDto(RegisterRequest registerRequest){
        if(registerRequest == null) {
            log.warn("User identity cannot be null");
            throw new InvalidInputException("User identity cannot be null");
        }
        if(userRepo.existsByUsername(registerRequest.getUsername())){
            log.warn("Username already exists!");
            throw new InvalidInputException("Username already exists!");
        }
        if(userRepo.existsByEmail(registerRequest.getEmail())){
            log.warn("Email already exists!");
            throw new InvalidInputException("Email already exists!");
        }
        if(registerRequest.getUsername().isEmpty()) {
            log.warn("Username cannot be empty");
            throw new InvalidInputException("Username cannot be empty");
        }
        if(registerRequest.getEmail().isEmpty()) {
            log.warn("Email cannot be empty");
            throw new InvalidInputException("Email cannot be empty");
        }
        if(registerRequest.getPassword().isEmpty()) {
            log.warn("Password cannot be empty");
            throw new InvalidInputException("Password cannot be empty");
        }

        if(!isValidPassword(registerRequest.getPassword())) {
            log.warn("Invalid password");
            throw new InvalidInputException("Invalid password");
        }
    }
    private boolean isValidPassword(String password){
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,128}$";
        if(!password.matches(regex)){
            log.warn("Password not strong enough");
            throw new InvalidInputException("Password not Strong");
        }
        if (password.length()<8){
            log.warn("Password must be atleast be 8 characters long");
            throw new InvalidInputException(("Password must atleast be 8 characters long"));
        }
        if (password.length()>128){
            log.warn("Password must be lower than 128 characters");
            throw new InvalidInputException(("Password must be lower than 128 characters"));
        }
        return true;
    }

}
