package com.nouba.app.services;

import com.nouba.app.dto.ActiveClientDTO;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.User;
import com.nouba.app.entities.Role;
import com.nouba.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nouba.app.repositories.AgencyRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;




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

    // Add to UserService.java
    public ApiResponse<List<ActiveClientDTO>> getActiveClientsThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);

        List<User> activeUsers = userRepository.findActiveUsersThisWeek(
                startOfWeek,
                now
        );

        List<ActiveClientDTO> result = activeUsers.stream()
                .map(this::convertToActiveClientDTO)
                .toList();

        return new ApiResponse<>(result, "Active clients this week retrieved", 200);
    }

    private ActiveClientDTO convertToActiveClientDTO(User user) {
        ActiveClientDTO.ActiveClientDTOBuilder builder = ActiveClientDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .timeAgo(calculateTimeAgo(user.getLastLogin()));


        // Add photo URL only for agencies
        if (user.getRole() == Role.AGENCY) {
            Agency agency = agencyRepository.findByUser(user)
                    .orElse(null);
            if (agency != null) {
                builder.photoUrl(agency.getPhotoUrl());
            }
        }

        return builder.build();
    }

    private String calculateTimeAgo(LocalDateTime lastLogin) {
        long minutes = ChronoUnit.MINUTES.between(lastLogin, LocalDateTime.now());
        if (minutes < 60) {
            return minutes + " minutes ago";
        }
        long hours = ChronoUnit.HOURS.between(lastLogin, LocalDateTime.now());
        if (hours < 24) {
            return hours + " hours ago";
        }
        long days = ChronoUnit.DAYS.between(lastLogin, LocalDateTime.now());
        return days + " days ago";
    }
}