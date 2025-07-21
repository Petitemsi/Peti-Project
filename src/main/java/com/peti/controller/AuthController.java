package com.peti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.model.ErrorResponse;
import com.peti.model.LoggedInUser;
import com.peti.model.User;
import com.peti.repository.UserRepository;
import com.peti.security.JwtHelper;
import com.peti.security.model.LoginRequest;
import com.peti.service.AuthService;
import com.peti.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtHelper helper;
    private final UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;
    @Autowired private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    private Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;

    //    private final JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.IM_USED)
                    .body(new ErrorResponse("Email already exists."));
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.IM_USED)
                    .body(new ErrorResponse("Username already exists."));
        }
        authService.register(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ErrorResponse("User Registered Successfully."));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        logger.info("Login attempt for email: {}", request.getUsername());
        try {
            // Validate user status and credentials
            userService.validateLoginAttempt(request.getUsername(), request.getPassword());

            // Proceed with authentication
            this.doAuthenticate(request.getUsername(), request.getPassword());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            User user = userService.getUserByUsername(request.getUsername());
            String token = this.helper.generateToken(userDetails);

            LoggedInUser loggedInUser = LoggedInUser.builder()
                    .jwtToken(token)
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .pictureUrl(user.getPictureUrl())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .enabled(user.isActive())
                    .build();
            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                    .httpOnly(true)  // Prevent XSS attacks
                    .secure(true)     // Enable in production (HTTPS only)
                    .path("/")
                    .maxAge(5 * 60 * 60) // 5 hours (matches your current setting)
                    .sameSite("Lax")     // Protection against CSRF
                    .build();
            // Serialize user object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(loggedInUser);

            // URL encode the JSON to ensure cookie validity
            String encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString());

            // Create user info cookie (non-httpOnly so client can access it)
            ResponseCookie userCookie = ResponseCookie.from("userInfo", encodedUserJson)
                    .httpOnly(false)  // Allow client-side access
                    .secure(true)
                    .path("/")
                    .maxAge(5 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            // Add cookies to the response
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());
            logger.info("JWT token created for email: {}", userDetails.getUsername());
            return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            logger.error("Login error for username: {}", request.getUsername(), e);
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponse(e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error during login for username: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during login."));
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request)
    {
        String email = request.get("email");
        if (email == null)
        {
            return ResponseEntity.badRequest().body("Email is required");
        }
        try
        {
            authService.initiateForgotPassword(email);
            return ResponseEntity.ok("OTP sent to your email");
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request)
    {
        String email = request.get("email");
        String otp = request.get("otp");
        if (email == null)
        {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (otp == null)
        {
            return ResponseEntity.badRequest().body("OTP is required");
        }
        try
        {
            if(authService.verifyOtp(email,otp))
            {
            return ResponseEntity.ok("OTP verified successfully");}
            else
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Invalid OTP."));
            }
        }
        catch (ResponseStatusException e)
        {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponse(e.getReason()));
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request)
    {
        String email = request.get("email");
        String password = request.get("newPassword");
        if (email == null)
        {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (password == null)
        {
            return ResponseEntity.badRequest().body("Password is required");
        }
        authService.resetPassword(email, password);
        return ResponseEntity.ok("Password reset successfully");

    }
    private void doAuthenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }
}