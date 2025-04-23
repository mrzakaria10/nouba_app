// AdminRegisterRequest.java
package com.nouba.app.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdminRegisterRequest {
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @Size(min = 8)
    private String password;

    @NotBlank
    private String phone;
}
