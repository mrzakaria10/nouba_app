package com.nouba.app.services;

import com.nouba.app.dto.ApiResponse;
import com.nouba.app.entities.User;
import com.nouba.app.entities.Role;
import com.nouba.app.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ApiResponse<>(users, "All users retrieved successfully", 200);
    }

    public ApiResponse<List<User>> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return new ApiResponse<>(users, "Users with role " + role + " retrieved successfully", 200);
    }

    public ApiResponse<String> deleteUser(Long id) {
        userRepository.deleteById(id);
        return new ApiResponse<>("User deleted successfully", 200);
    }
}