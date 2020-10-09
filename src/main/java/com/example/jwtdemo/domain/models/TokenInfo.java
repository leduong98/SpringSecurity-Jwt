package com.example.jwtdemo.domain.models;

import lombok.Data;

@Data
public class TokenInfo {

    private String email;
    private String password;

}
