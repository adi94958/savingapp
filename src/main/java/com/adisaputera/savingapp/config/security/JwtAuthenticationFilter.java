package com.adisaputera.savingapp.config.security;

import com.adisaputera.savingapp.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromRequest(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(token, request);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            if (!jwtService.isValidAccessToken(token)) {
                log.warn("Invalid or expired access token");
                return;
            }

            JwtService.JwtUserInfo userInfo = jwtService.extractUserInfo(token);
            
            if (userInfo.getUserId() == null) {
                log.warn("User ID not found in token");
                return;
            }

            setAuthenticationContext(userInfo, request);
            
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthenticationContext(JwtService.JwtUserInfo userInfo, HttpServletRequest request) {
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + userInfo.getRole())
        );

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userInfo.getUserId(), null, authorities
        );
        
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("User authenticated: {} with role: {}", userInfo.getUserId(), userInfo.getRole());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }
}