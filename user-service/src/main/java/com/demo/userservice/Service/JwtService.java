package com.demo.userservice.Service;

import com.demo.userservice.Repositories.UserRepo;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JwtService {
    private final UserRepo userRepo;

    public JwtService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    private String jwtSecret = System.getenv("JWT_SECRET");

    private Long jwtExpiration = 360000L;
    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        log.info("username : {}",username);
        Date currentDate = new Date();
        Long userId = userRepo.findUserByUsername(username).getId();
        Date expirationTime = new Date(currentDate.getTime() + jwtExpiration);
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", roles.get(0))
                .claim("userId",Long.toString(userId))
                .setIssuedAt(currentDate)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
    public String getUsernameFromToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return  claims.getSubject();
    }
    public boolean validateToken(String token){
        log.info("Validating token ....");
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        }
        catch (ExpiredJwtException e){
            throw new ExpiredJwtException(e.getHeader(),e.getClaims(),"Jwt token is expired");
        }
        catch (InvalidClaimException e){
            throw new JwtException("Jwt token is invalid");
        }
        catch (Exception e){
            throw new RuntimeException("Something went wrong with the jwt token validation");
        }

    }
    public void expireToken(String token){
        Date currentDate = new Date();
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        claims.setExpiration(new Date(currentDate.getTime()));
    }
    public String getJWTFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

}
