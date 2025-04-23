package com.nouba.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyUpdateDTO {
    private String name;
    private String address;
    private String phone;
    private String email;

    // User fields that might need updating
    //private String userEmail;
    //private String userName;
}