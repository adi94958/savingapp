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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip if no Authorization header or doesn't start with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7); // Remove "Bearer " prefix

            // Validate if it's a valid access token
            if (!jwtService.isValidAccessToken(jwt)) {
                log.warn("Invalid or expired access token");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user information from token
            JwtService.JwtUserInfo userInfo = jwtService.extractUserInfo(jwt);

            // Check if user is already authenticated
            if (userInfo.getUserId() != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Create authorities based on user role
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + userInfo.getRole())
                );

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userInfo.getUserId(),
                        null,
                        authorities
                );

                // Add request details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Add user info to request attributes for easy access in controllers
                request.setAttribute("userId", userInfo.getUserId());
                request.setAttribute("fullName", userInfo.getFullName());
                request.setAttribute("userRole", userInfo.getRole());

                log.debug("User authenticated successfully: {} with role: {}",
                        userInfo.getUserId(), userInfo.getRole());
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip JWT filter for public endpoints
        return path.startsWith("/api/auth/") ;
    }
}