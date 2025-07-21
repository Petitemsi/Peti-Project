package com.peti.service;

import com.peti.constants.Role;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setApproved(true);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
    }

    @Test
    void updateActiveStatus_WhenUserExists_ShouldUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateActiveStatus(1L, false);

        assertFalse(result.isActive());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateActiveStatus_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateActiveStatus(1L, false));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found.", exception.getReason());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateApprovalStatus_WhenUserExists_ShouldUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateApprovalStatus(1L, false);

        assertFalse(result.isApproved());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void unlockAccount_WhenUserExists_ShouldUnlockAndReturnUser() {
        user.setLocked(true);
        user.setFailedLoginAttempts(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.unlockAccount(1L);

        assertFalse(result.isLocked());
        assertEquals(0, result.getFailedLoginAttempts());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getActiveUsers_ShouldReturnActiveUsers() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findByIsLockedAndIsApproved(false, true)).thenReturn(users);

        List<User> result = userService.getActiveUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByIsLockedAndIsApproved(false, true);
    }

    @Test
    void getNotApprovedUsers_ShouldReturnNotApprovedUsers() {
        user.setApproved(false);
        List<User> users = Arrays.asList(user);
        when(userRepository.findByIsApproved(false)).thenReturn(users);

        List<User> result = userService.getNotApprovedUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByIsApproved(false);
    }

    @Test
    void getLockedUsers_ShouldReturnLockedUsers() {
        user.setLocked(true);
        List<User> users = Arrays.asList(user);
        when(userRepository.findByIsLocked(true)).thenReturn(users);

        List<User> result = userService.getLockedUsers();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByIsLocked(true);
    }

    @Test
    void getActiveUsersCount_ShouldReturnCount() {
        when(userRepository.countByIsActiveAndIsApproved(true, true)).thenReturn(10L);

        long result = userService.getActiveUsersCount();

        assertEquals(10L, result);
        verify(userRepository, times(1)).countByIsActiveAndIsApproved(true, true);
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void changePassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(user, "currentPassword", "newPassword");

        assertEquals("newEncodedPassword", user.getPassword());
        verify(passwordEncoder, times(1)).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.changePassword(user, "wrongPassword", "newPassword"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Current password is incorrect", exception.getReason());
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_WithShortNewPassword_ShouldThrowException() {
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.changePassword(user, "currentPassword", "short"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("New password must be at least 8 characters long", exception.getReason());
        verify(passwordEncoder, times(1)).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
}