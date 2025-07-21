package com.peti.controller;

import com.peti.engine.PetiEngine;
import com.peti.model.ErrorResponse;
import com.peti.model.ProfilePictureResponse;
import com.peti.model.User;
import com.peti.service.CloudinaryService;
import com.peti.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final PetiEngine petiEngine;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/not-approved")
    public ResponseEntity<List<User>> getNotApprovedUsers() {
        return ResponseEntity.ok(userService.getNotApprovedUsers());
    }

    @GetMapping("/locked")
    public ResponseEntity<List<User>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    @PostMapping("/{userId}/toggle-active")
    public ResponseEntity<User> toggleActiveStatus(@PathVariable Long userId, @RequestBody boolean isActive) {
        return ResponseEntity.ok(userService.updateActiveStatus(userId, isActive));
    }

    @PostMapping("/{userId}/toggle-approval")
    public ResponseEntity<User> toggleApprovalStatus(@PathVariable Long userId, @RequestBody boolean isApproved) {
        return ResponseEntity.ok(userService.updateApprovalStatus(userId, isApproved));
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<User> unlockAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.unlockAccount(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getActiveItemsCount() {
        long count = userService.getActiveUsersCount();
        return ResponseEntity.ok(count);
    }

    @PostMapping(value = "/profilePicture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            // Get the authenticated user
            User user = petiEngine.getLoggedInUser();

            // Validate file
            if (profilePicture == null || profilePicture.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("No profile picture provided."));
            }

            // Upload image to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(profilePicture);

            // Update user's profile picture URL
            user.setPictureUrl(imageUrl);
            userService.updateUser(user);

            // Return the updated profile picture URL
            return ResponseEntity.ok(new ProfilePictureResponse(imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error uploading profile picture: " + e.getMessage()));
        }
    }

    @PostMapping("/changeInfo")
    public ResponseEntity<?> changeInfo(@RequestBody Map<String, String> request) {
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String email = request.get("email");
        User user = petiEngine.getLoggedInUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userService.updateUser(user);
        return ResponseEntity.ok("Profile Updated successfully");

    }
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        try {
            User user = petiEngine.getLoggedInUser();
            userService.changePassword(user, currentPassword, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error changing password: " + e.getMessage()));
        }
    }
}