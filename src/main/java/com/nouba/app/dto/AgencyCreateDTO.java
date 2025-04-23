package com.nouba.app.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgencyCreateDTO {

    @Getter
    @Setter

        @NotBlank
        private String name;
        @NotBlank
        private String address;
        @NotBlank
        private String phone;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 8)
        private String password;

}
