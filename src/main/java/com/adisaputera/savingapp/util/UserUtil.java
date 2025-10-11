package com.adisaputera.savingapp.util;

import com.adisaputera.savingapp.exception.ResourceNotFoundException;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class UserUtil {
    public static User getCurrentLoggedInUser(UserRepository userRepository) {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        UUID userId = UUID.fromString(currentUserId);
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        
        return userOptional.get();
    }
}