package com.example.jwtdemo.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.jwtdemo.domain.repositories.UserRepository;
import org.apache.commons.lang.RandomStringUtils;

public class BaseService {

    @Autowired
    protected UserRepository userRepository;

    protected String generateToken() {
        String token = RandomStringUtils.randomAlphabetic(8);
        return token;
    }

    protected String genRefreshToken(){
        String token = RandomStringUtils.randomAlphabetic(25);
        return token;
    }

}
