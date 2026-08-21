package com.dau.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Chào bạn khóa 24CT đến với học phần CNPM-DAU";
    }
}