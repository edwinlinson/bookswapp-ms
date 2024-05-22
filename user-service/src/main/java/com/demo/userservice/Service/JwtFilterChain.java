package com.demo.userservice.Service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilterChain extends OncePerRequestFilter {
	
	@Autowired
	private JwtService jwtService;
	@Autowired
	private UserDetailsService detailsService;
	
	private final Logger logger = LoggerFactory.getLogger(JwtFilterChain.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		logger.debug("Uri called is {}" , request.getRequestURI());
		try {
			String jwt = parseJwt(request);
			if(jwt != null && jwtService.validateToken(jwt)) {
				String username = jwtService.getUsernameFromToken(jwt);
				UserDetails details = detailsService.loadUserByUsername(username);
				
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, 
						null, details.getAuthorities());
				
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				SecurityContextHolder.getContext().setAuthentication(auth);
					
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}
		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String jwt = jwtService.getJWTFromRequest(request);
		logger.debug("jwt token parsed is :" +jwt);
		return jwt;
	}

}
