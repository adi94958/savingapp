package com.adisaputera.savingapp.service;

import com.adisaputera.savingapp.dto.message.ApiResponse;
import com.adisaputera.savingapp.dto.message.MetadataResponse;
import com.adisaputera.savingapp.dto.request.ChangePasswordRequestDTO;
import com.adisaputera.savingapp.dto.request.CreateNasabahRequestDTO;
import com.adisaputera.savingapp.dto.request.UpdateNasabahRequestDTO;
import com.adisaputera.savingapp.model.RoleEnum;
import com.adisaputera.savingapp.model.User;
import com.adisaputera.savingapp.repository.UserRepository;
import com.adisaputera.savingapp.util.UserUtil;

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

    public ApiResponse<UserResponseDTO> getProfile() {
        User user = UserUtil.getCurrentLoggedInUser(userRepository);
        
        UserResponseDTO userDto = UserResponseDTO.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build();
        
        return ApiResponse.success("User profile retrieved successfully", userDto);
    }

    public ApiResponse<String> changePassword(ChangePasswordRequestDTO request) {
        User user = UserUtil.getCurrentLoggedInUser(userRepository);
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password changed successfully", null);
    }

    public ApiResponse<UserResponseDTO> createNasabah(CreateNasabahRequestDTO request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.nasabah) 
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
        User savedUser = userRepository.save(user);

        UserResponseDTO userDto = (UserResponseDTO.builder()
                .id(savedUser.getId().toString())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .address(savedUser.getAddress())
                .phone(savedUser.getPhone())
                .createdAt(savedUser.getCreatedAt().toString())
                .build());

        return ApiResponse.success("Nasabah added successfully", userDto);
    }

    public ApiResponse<List<UserResponseDTO>> getNasabahList(int page, int perPage, String sortBy, String sortDirection, String keyword) {
        int pageIndex = page > 0 ? page -1 : 0;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageIndex, perPage, sort);

        Page<User> userPage;
        if (StringUtils.hasText(keyword)) {
            userPage = userRepository.findByRoleAndFullNameContainingIgnoreCase(RoleEnum.nasabah, keyword, pageable);
        } else {
            userPage = userRepository.findByRole(RoleEnum.nasabah, pageable);
        }

        List<UserResponseDTO> userDtos = userPage.getContent().stream()
            .map(user -> UserResponseDTO.builder()
                    .id(user.getId().toString())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
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

        return ApiResponse.success("Nasabah list retrieved successfully", userDtos, metadata);
    }

    public ApiResponse<UserResponseDTO> getNasabahById(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Nasabah not found");
        }

        User user = userOptional.get();
        
        UserResponseDTO userDto = UserResponseDTO.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build();
        return ApiResponse.success("Nasabah retrieved successfully", userDto);
    }

    public ApiResponse<UserResponseDTO> updateProfile(UpdateNasabahRequestDTO request) {
        User user = UserUtil.getCurrentLoggedInUser(userRepository);
        return updateUser(user.getId(), request);
    }

    public ApiResponse<UserResponseDTO> updateNasabah(UUID userId, UpdateNasabahRequestDTO request) {
        return updateUser(userId, request);
    }   

    public ApiResponse<UserResponseDTO> updateUser(UUID userId, UpdateNasabahRequestDTO request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = userOptional.get();
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        UserResponseDTO userDto = UserResponseDTO.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build();
        return ApiResponse.success("User updated successfully", userDto);
    }

    public ApiResponse<String> deleteNasabah(UUID userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        User user = userOptional.get();

        if (user.getRole() != RoleEnum.nasabah) {
            throw new BadRequestException("Only 'nasabah' users can be deleted through this endpoint");
        }
        
        userRepository.deleteById(userId);
        return ApiResponse.success("Nasabah deleted successfully", null);
    }
}