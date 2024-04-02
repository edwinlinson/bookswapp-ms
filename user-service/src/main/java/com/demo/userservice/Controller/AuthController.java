package com.demo.userservice.Controller;

import com.demo.userservice.Exceptions.InvalidInputException;
import com.demo.userservice.Model.Entity.User;
import com.demo.userservice.Model.dto.OtpDto;
import com.demo.userservice.Model.dto.request.LoginRequest;
import com.demo.userservice.Model.dto.request.RegisterRequest;
import com.demo.userservice.Model.dto.response.GenerateOtpResponse;
import com.demo.userservice.Model.dto.response.LoginResponse;
import com.demo.userservice.Model.dto.response.RegisterResponse;
import com.demo.userservice.Repositories.OtpRepo;
import com.demo.userservice.Repositories.UserRepo;
import com.demo.userservice.Service.EmailSenderService;
import com.demo.userservice.Service.JwtService;
import com.demo.userservice.Service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin("*")
public class AuthController {
   
	
	@Autowired
	private EmailSenderService emailSenderService;
	


    @GetMapping("/hello")
    public String hello(){
        return "hello spring working";
    }
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final OtpRepo otpRepo;
    private final UserRepo repo;

    public AuthController(UserRepo repo,OtpRepo otpRepo,HttpSession httpSession,AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.otpRepo = otpRepo;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.repo = repo;
    }
    @PostMapping("/google/oauth/login")
    public ResponseEntity<LoginResponse> oauthLogin(@RequestBody String oauthToken){
        String[] chunks = oauthToken.split("\\.");
        String payload = new String(Base64.getDecoder().decode(chunks[1]));
        JSONObject payloadJson = new JSONObject(payload);
        String email = payloadJson.getString("email");
        String name = payloadJson.getString("name");
        String picture = payloadJson.getString("picture");
        if(!userService.emailAlreadyUsed(email)){
            userService.registerOathUser(email,name,picture);
        }
        String username = userService.getUsernameFromEmail(email);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        String token = jwtService.generateToken(authToken);
        return ResponseEntity.ok(
                LoginResponse.builder()
                        .message("Login successful!")
                        .accessToken(token)
                        .build()
        );
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest userLoginRequestDto){
        String username = userLoginRequestDto.getUsername();
        String password = userLoginRequestDto.getPassword();
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        }
        catch (AuthenticationException e){
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .message("Invalid username or password!")
                            .build());
        }
        catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(LoginResponse.builder()
                            .message("Something went wrong while authenticating user")
                            .build());
        }
        String token;
        try {
            token = jwtService.generateToken(authentication);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(
                    LoginResponse.builder()
                            .message("Something went wrong while generating token")
                            .build()
            );
        }
        return ResponseEntity.ok(
                LoginResponse.builder()
                        .message("Authentication Successful!")
                        .accessToken(token)
                        .build()
        );
    }
    @PostMapping("/generateOTPF")
    public String genOTPF(@RequestBody String email) {
    	System.out.println("In generat eF method");
    	String otp = generateOTP();
    	emailSenderService.sendEmail(email, "Otp for Bookswapp app", "Your OTP is: " + otp+ "   Please donot this  share with anyone. ");
    	return otp;
    }
    
    @PostMapping("/generateOTP")
    public ResponseEntity<GenerateOtpResponse> genOtp(@RequestBody RegisterRequest userRegisterDto){
    	System.out.println("In genertae otp");
    	String otp = generateOTP();
    	System.out.println(" "+otp);
    	emailSenderService.sendEmail(userRegisterDto.getEmail(), "Otp for Bookswapp app", "Your OTP is: " + otp+ "   Please donot share with anyone");
    	 
    	com.demo.userservice.Model.Entity.OtpDto existingOtp = otpRepo.findByEmail(userRegisterDto.getEmail());
        if (existingOtp != null) {
            otpRepo.delete(existingOtp);
        }
        
    	com.demo.userservice.Model.Entity.OtpDto dto =new com.demo.userservice.Model.Entity.OtpDto();
    	 dto.setEmail(userRegisterDto.getEmail());
    	 dto.setOtp(otp);
    	otpRepo.save(dto);
    	 
        GenerateOtpResponse response = new GenerateOtpResponse();
        response.setMessage("OTP generated successfully");
        response.setRegisterRequest(userRegisterDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest userRegisterDto){
      String token;
      try {
    	com.demo.userservice.Model.Entity.OtpDto otp = otpRepo.findByEmail(userRegisterDto.getEmail());
    	System.out.println(" saved otp: "+otp.getOtp()+" typed otp :"+userRegisterDto.getOtp());
      	
    	if(otp != null && otp.getOtp().equals(userRegisterDto.getOtp())) {
    		userService.registerUser(userRegisterDto);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userRegisterDto.getUsername());
            UsernamePasswordAuthenticationToken  authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            token = jwtService.generateToken(authToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);
    	}else {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RegisterResponse.builder()
                    .message("Invalid OTP Please Try Again! ").build());
    	}
      }
      catch (InvalidInputException e){
          return ResponseEntity.badRequest().body(RegisterResponse.builder()
                  .message(e.getMessage()).build());
      }
      catch (Exception e){
          return ResponseEntity.internalServerError().body(RegisterResponse.builder()
                  .message(e.getMessage()).build());
      }
      return ResponseEntity.ok(RegisterResponse
              .builder()
              .message("Registration Successful")
              .accessToken(token)
              .build());
  }
  
//  @PostMapping("/fetchUserDetails")
//  public ResponseEntity<RegisterRequest> fetchUserDetails(@RequestBody String email){
//  	User user = repo.findByEmail(email);
//  	 if (user != null) {
//           RegisterRequest userDetails = new RegisterRequest(user.getEmail(), user.getUsername(), user.getName());
//           return ResponseEntity.ok(userDetails);
//       } else {
//           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//       }
//  }
  
  @PostMapping("/fetchUserDetails")
  public ResponseEntity<RegisterRequest> fetchUserDetails(@RequestBody Map<String, String> request) {
	  System.out.println(" "+request);
      String email = request.get("email");
      User user = repo.findByEmail(email);
      if (user != null) {
    	  System.out.println(" "+user.getEmail());
          RegisterRequest userDetails = new RegisterRequest(user.getEmail(), user.getUsername(), user.getName());
          System.out.println(" User details: "+userDetails);
          return ResponseEntity.ok(userDetails);
      } else {
          return ResponseEntity.notFound().build();
      }
  }
    
    
    
//    @PostMapping("/register")
//    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest userRegisterDto){
//        String token;
//        try {
//        	System.out.println("stored : "+userRegisterDto.getStore()+"  user entered otp :"+userRegisterDto.getOtp());
//        	if(userRegisterDto.getOtp().equals(userRegisterDto.getStore())) {
//        		userService.registerUser(userRegisterDto);
//                UserDetails userDetails = userDetailsService.loadUserByUsername(userRegisterDto.getUsername());
//                UsernamePasswordAuthenticationToken  authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//                token = jwtService.generateToken(authToken);
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//        	}
//        	userService.registerUser(userRegisterDto);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(userRegisterDto.getUsername());
//            UsernamePasswordAuthenticationToken  authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
//            token = jwtService.generateToken(authToken);
//            SecurityContextHolder.getContext().setAuthentication(authToken);
////        	userService.registerUser(userRegisterDto);
////            UserDetails userDetails = userDetailsService.loadUserByUsername(userRegisterDto.getUsername());
////            UsernamePasswordAuthenticationToken  authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
////            token = jwtService.generateToken(authToken);
////            SecurityContextHolder.getContext().setAuthentication(authToken);
//        }
//        catch (InvalidInputException e){
//            return ResponseEntity.badRequest().body(RegisterResponse.builder()
//                    .message(e.getMessage()).build());
//        }
//        catch (Exception e){
//            return ResponseEntity.internalServerError().body(RegisterResponse.builder()
//                    .message(e.getMessage()).build());
//        }
//        return ResponseEntity.ok(RegisterResponse
//                .builder()
//                .message("Registration Successful")
//                .accessToken(token)
//                .build());
//    }
    
    public static String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
