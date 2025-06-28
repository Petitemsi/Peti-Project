package com.peti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/wardrobe";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "redirect:/wardrobe";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
} 