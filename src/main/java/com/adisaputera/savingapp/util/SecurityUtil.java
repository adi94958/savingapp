package com.adisaputera.savingapp.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

public class SecurityUtil {

    /**
     * Get current authenticated user ID
     */
    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of((String) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    /**
     * Get current authenticated user authorities/roles
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        }
        return null;
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser");
    }

    /**
     * Check if current user is admin/staff
     */
    public static boolean isStaff() {
        return hasRole("staff");
    }

    /**
     * Check if current nasabah is regular user
     */
    public static boolean isNasabah() {
        return hasRole("nasabah");
    }
}