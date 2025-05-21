package com.nouba.app.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActiveClientDTO {
    private String name;
    private String email;
    private String role;
    private String phone;  // Added phone number
    private String photoUrl; // Only for AGENCY role
    private String timeAgo; // e.g. "5 minutes ago"
}