package com.peti.engine;


import com.peti.model.User;
import com.peti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class PetiEngine {
    @Autowired
    private UserRepository userRepository;

    public User getLoggedInUser()
    {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails)
        {
            String username = ((UserDetails) principal).getUsername();
            // Assuming email is used as username in your system
            return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new RuntimeException("No authenticated user found");
    }
}
