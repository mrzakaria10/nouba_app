package com.nouba.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBasicInfoDTO {
    private Long id;  // This will now be agencyId or clientId based on role
    private String name;
    private String email;
    private String role;
    private String phone;
}