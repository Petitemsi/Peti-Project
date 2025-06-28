package com.peti.controller;

import com.peti.model.User;
import com.peti.security.JwtService;
import com.peti.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              Model model) {
        try {
            log.info("Registering user: {}", username);
            userService.registerUser(username, email, password);
            log.info("User registered successfully: {}", username);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            log.error("Registration failed for user: {}", username, e);
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            log.info("Login attempt for user: {}", username);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            
            log.info("Login successful for user: {}", username);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public String loginForm(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        try {
            log.info("Form login attempt for user: {}", username);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            
            log.info("Form login successful for user: {}", username);
            
            // Store token in session or redirect with token
            return "redirect:/dashboard?token=" + token + "&username=" + user.getUsername();
        } catch (Exception e) {
            log.error("Form login failed", e);
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
} 