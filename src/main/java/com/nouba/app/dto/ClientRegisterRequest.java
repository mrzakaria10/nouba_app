package com.nouba.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ClientRegisterRequest {
    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
    @NotBlank
    private String phone;
    @NotBlank
    private String address;

}

