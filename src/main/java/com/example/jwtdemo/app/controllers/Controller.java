package com.example.jwtdemo.app.controllers;

import com.example.jwtdemo.app.dtos.LoginRequest;
import com.example.jwtdemo.app.dtos.RegisterRequest;
import com.example.jwtdemo.app.dtos.TokenRequest;
import com.example.jwtdemo.domain.models.TokenInfo;
import com.example.jwtdemo.domain.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest){
        return userService.register(registerRequest);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest refreshToken){
        return  userService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String accessToken,
                                    @RequestBody TokenRequest refreshToken){
        return userService.logout(refreshToken);
    }

}
