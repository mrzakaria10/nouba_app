package com.nouba.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AgencyUpdateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private MultipartFile photo; // Optional photo update

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Email should be valid")
    private String email;
    @NotNull
    private Long cityId; // Changed from primitive long to Long for proper null checking

    //private String PhotoUrl;
}