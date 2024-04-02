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

    private String jwtSecret = "3b54477744fe2a57d708a79333877c40e9ffe9d5337b803f91e722bee5dd71e2202cb5f6bda7b988ebe32c7ced69d97a3d72e2229634490ddccd5e349e1d3c5b0a8e36afcaf7a0abe2c26155958234c10079a171206dcaca178706a716204d041bdeea020371db6cd45468bb231e7d7936e2b24d459cc7f13b55c1815006055587779ebefb72a539a08d87568d5316dec0db23da3f7db66699595e9b57de6da3f4ce31d403e84258f364772e16a84d8da871b6b7079e0da94caa6f030aaa91e0330d0744eac14afcc070fa1d28e324017c04942fb8e3dae3b8ce853e5fa354bf0edac5f77fbf9c77b550fa4feeb313cf233b1fab79d2c70d05ec4bf0ccb24211";

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
