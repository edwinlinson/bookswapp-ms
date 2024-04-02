package com.demo.userservice.Service.Security;

import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Repositories.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!userRepo.existsByUsername(username)){
            throw new UsernameNotFoundException("User with this username does not exist");
        }
        User user = userRepo.findUserByUsername(username);
        return new CustomUserDetails(user);
    }
}
