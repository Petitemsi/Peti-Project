package com.peti.service;

import com.peti.model.User;
import com.peti.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    public void validateLoginAttempt(String username, String rawPassword) {
        User user = getUserByUsername(username);

        if (!user.isApproved()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not approved. Please wait for admin approval.");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled. Please contact the administrator.");
        }

        if (user.isLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked due to multiple failed login attempts.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= 3) {
                user.setLocked(true);
                userRepository.save(user);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked due to 3 failed login attempts.");
            }

            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public User updateActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        user.setActive(isActive);
        return userRepository.save(user);
    }

    public User updateApprovalStatus(Long userId, boolean isApproved) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        user.setApproved(isApproved);
        return userRepository.save(user);
    }

    public User unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsLockedAndIsApproved( false, true);
    }

    public List<User> getNotApprovedUsers() {
        return userRepository.findByIsApproved(false);
    }

    public List<User> getLockedUsers() {
        return userRepository.findByIsLocked(true);
    }
    public long getActiveUsersCount() {
        return userRepository.countByIsActiveAndIsApproved(true, true);
    }
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        userRepository.delete(user);
    }
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters long");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}