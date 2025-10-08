package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.request.ChangePasswordRequestDTO;
import com.adisaputera.savingapp.dto.request.UserCreateRequestDTO;
import com.adisaputera.savingapp.dto.request.UserUpdateRequestDTO;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.adisaputera.savingapp.dto.response.UserResponseDTO;
import com.adisaputera.savingapp.exception.BadRequestException;
import com.adisaputera.savingapp.exception.DuplicateResourceException;
import com.adisaputera.savingapp.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<UserResponseDTO> createUser(UserCreateRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.valueOf(request.getRole()))
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
        User savedUser = userRepository.save(user);

        UserResponseDTO userDto = (UserResponseDTO.builder()
                .id(savedUser.getId().toString())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .address(savedUser.getAddress())
                .phone(savedUser.getPhone())
                .createdAt(savedUser.getCreatedAt().toString())
                .build());

        return ApiResponse.success("User added successfully", userDto);
    }

    public ApiResponse<List<UserResponseDTO>> getUserList(int page, int perPage, String sortBy, String sortDirection, String keyword) {
        int pageIndex = page > 0 ? page -1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        Page<User> userPage;
        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByFullNameContainingIgnoreCase(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponseDTO> userDtos = userPage.getContent().stream()
            .map(user -> UserResponseDTO.builder()
                    .id(user.getId().toString())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .address(user.getAddress())
                    .phone(user.getPhone())
                    .createdAt(user.getCreatedAt().toString())
                    .build()
                )
            .collect(Collectors.toList());

        MetadataResponse metadata = MetadataResponse.builder()
                .page(userPage.getNumber() + 1)
                .size(userPage.getSize())
                .total(userPage.getTotalPages())
                .build();

        return ApiResponse.success("User list retrieved successfully", userDtos, metadata);
    }

    public ApiResponse<UserResponseDTO> getUserById(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new DuplicateResourceException("User", "id", userOptional.get().getId());
        }
        User user = userOptional.get();
        UserResponseDTO userDto = UserResponseDTO.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build(
        );
        return ApiResponse.success("User retrieved successfully", userDto);
    }

    public ApiResponse<UserResponseDTO> updateUser(UUID userId, UserUpdateRequestDTO request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new DuplicateResourceException("User", "id", userOptional.get().getId());
        }
        User user = userOptional.get();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(RoleEnum.valueOf(request.getRole()));
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        UserResponseDTO userDto = UserResponseDTO.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build();
        return ApiResponse.success("User updated successfully", userDto);
    }

    public ApiResponse<String> changePassword(UUID userId, ChangePasswordRequestDTO request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
           throw new DuplicateResourceException("User", "id", userOptional.get().getId());
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password changed successfully", null);
    }

    public ApiResponse<String> deleteUser(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userOptional.get().getId());
        }
        userRepository.deleteById(userId);
        return ApiResponse.success("User deleted successfully", null);
    }
}