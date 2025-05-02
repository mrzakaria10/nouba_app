package com.nouba.app.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyCreateDTO {
        @NotBlank(message = "Name is required")
        private String name;
        /**
        @NotBlank(message = "Photo URL is required")
        private String photoUrl; // URL de l'image stockée
         */

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        private String phone;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotNull(message = "City ID is required")
        private Long cityId;
}