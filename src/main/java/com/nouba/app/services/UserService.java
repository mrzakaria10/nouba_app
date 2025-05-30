package com.nouba.app.services;

import com.nouba.app.dto.ActiveClientDTO;
import com.nouba.app.dto.ApiResponse;
import com.nouba.app.dto.UserBasicInfoDTO;
import com.nouba.app.entities.Agency;
import com.nouba.app.entities.User;
import com.nouba.app.entities.Role;
import com.nouba.app.repositories.ClientRepository;
import com.nouba.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nouba.app.repositories.AgencyRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final ClientRepository clientRepository;





    public ApiResponse<List<UserBasicInfoDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserBasicInfoDTO> result = users.stream()
                .map(user -> {
                    Long entityId = null;
                    if (user.getRole() == Role.AGENCY && user.getAgency() != null) {
                        entityId = user.getAgency().getId();
                    } else if (user.getRole() == Role.CLIENT && user.getClient() != null) {
                        entityId = user.getClient().getId();
                    }

                    return UserBasicInfoDTO.builder()
                            .id(entityId) // Will be agencyId or clientId
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .phone(user.getPhone())
                            .build();
                })
                .toList();

        return new ApiResponse<>(result, "All users retrieved successfully", 200);
    }
    public ApiResponse<List<User>> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return new ApiResponse<>(users, "Users with role " + role + " retrieved successfully", 200);
    }

    public ApiResponse<String> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Delete associated Client if exists
        if (user.getClient() != null) {
            clientRepository.delete(user.getClient());
        }

        // Delete associated Agency if exists
        if (user.getAgency() != null) {
            agencyRepository.delete(user.getAgency());
        }

        // Now delete the user
        userRepository.delete(user);

        return new ApiResponse<>("User and associated records deleted successfully", 200);
    }

    // Add to UserService.java
    public ApiResponse<List<ActiveClientDTO>> getActiveClientsThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);

        List<User> activeUsers = userRepository.findActiveUsersThisWeek(startOfWeek, now);

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
                .timeAgo(formatTimeAgo(user.getActivatedAt()));  // Changed to use activatedAt

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

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + " seconds ago";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minutes ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hours ago";
        }
        long days = hours / 24;
        return days + " days ago";
    }

}