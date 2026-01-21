package com.example.mubu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerTestController {

    @GetMapping("/swagger-test")
    public String test() {
        return "swagger ok";
    }
}
