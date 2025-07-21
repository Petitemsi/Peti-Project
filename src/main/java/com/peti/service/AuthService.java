package com.peti.service;


import com.peti.constants.Role;
import com.peti.model.OtpData;
import com.peti.model.User;
import com.peti.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;
    private Map<String, OtpData> otpStore = new HashMap<>();
    @Autowired
    private EmailService emailService;

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setActive(true);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
    public void initiateForgotPassword(String email) throws MessagingException {
        if (!userRepository.existsByEmail(email))
        {
            throw new RuntimeException("Invalid email: No user found with this email");
        }
        String otp = generateOtp();
        OtpData otpData = new OtpData();
        otpData.setOtp(otp);
        otpData.setExpiry(LocalDateTime.now().plusMinutes(10));

        otpStore.put(email, otpData);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp)
    {
        OtpData otpData = otpStore.get(email);
        if (otpData == null)
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No OTP request found for this email");
        }

        if (LocalDateTime.now().isAfter(otpData.getExpiry()))
        {
            otpStore.remove(email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "OTP has expired");
        }

        if (!otpData.getOtp().equals(otp))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid OTP");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        otpStore.remove(email);
        return true;
    }
    public void resetPassword(String email, String password)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(encoder.encode(password));
        userRepository.save(user);
    }
    public User getUserByEmail(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));


        return user;
    }
    public User getUserByUsername(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        return user;
    }
    private String generateOtp()
    {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
//    public void changePassword(String oldPassword, String newPassword)
//    {
//        User currentUser = sieberEngine.getLoggedInUser();
//        if (!encoder.matches(oldPassword, currentUser.getPassword()))
//        {
//            throw new RuntimeException("Old password is incorrect");
//        }
//        String encodedNewPassword = encoder.encode(newPassword);
//        currentUser.setPassword(encodedNewPassword);
//        userRepository.save(currentUser);
//    }


//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    public void approveUser(Long id) {
//        userRepository.findById(id).ifPresent(u -> {
//            u.setEnabled(true);
//            userRepository.save(u);
//        });
//    }
}