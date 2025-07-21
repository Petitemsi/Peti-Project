package com.peti.service;

import com.peti.constants.Role;
import com.peti.model.OtpData;
import com.peti.model.User;
import com.peti.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private OtpData otpData;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        otpData = new OtpData();
        otpData.setOtp("123456");
        otpData.setExpiry(LocalDateTime.now().plusMinutes(10));
    }

    @Test
    void register_ShouldSaveUserWithEncodedPassword() {
        when(passwordEncoder.encode("encodedPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.register(user);

        assertEquals(user, result);
        assertTrue(result.isActive());
        assertEquals(Role.USER, result.getRole());
        verify(passwordEncoder, times(1)).encode("encodedPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPassword_WhenUserExists_ShouldUpdatePassword() {
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.resetPassword("testuser@example.com", "newPassword");

        assertEquals("newEncodedPassword", user.getPassword());
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(user));

        User result = authService.getUserByEmail("testuser@example.com");

        assertEquals(user, result);
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = authService.getUserByUsername("testuser");

        assertEquals(user, result);
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}