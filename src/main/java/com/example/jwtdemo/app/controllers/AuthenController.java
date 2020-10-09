package com.example.jwtdemo.app.controllers;

import com.example.jwtdemo.domain.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthenController {

    @Autowired
    private UserService userService;

    @GetMapping("/v1/test")
    public String testToken(@RequestHeader String accessToken){
        return "oke";
    }

    @GetMapping("/v1/user/State")
    public ResponseEntity<?> activeAcc(@RequestParam String token){
        return userService.changeState(token);
    }

}
