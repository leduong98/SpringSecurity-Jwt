package com.example.jwtdemo.domain.utils;

public class Const {

    public static final String PREFIX = "cz ";

    public static String genMailKey(String token)
    {
        return PREFIX + "http://localhost:8080/api/v1/user/State?token=" + token;
    }

}
