package com.example.filterandinterceptor.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @GetMapping("/hellFilter")
    public String helloFilter() {
        return "Hello World";

    }
}
