package com.example.mubu.controller.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/internal/env")
public class EnvTestController {

    @Value("${gemini.api-key}")
    private String geminiKey;

    @GetMapping("/test")
    public String test() {
        return geminiKey.substring(0, 5);
    }
}
