package com.peti.engine;



import com.peti.constants.Role;
import com.peti.model.User;
import com.peti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public void loadSuperAdmin()
    {

        try
        {
            User user = new User();
            user.setUsername("Admin");
            user.setFirstName("Sami");
            user.setLastName("Wilson");
            user.setApproved(true);
            user.setEmail("admin@gmail.com");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setRole(Role.ADMIN);
            if (!userRepository.existsByEmail(user.getEmail()))
            {
                userRepository.save(user);
            }
        }
        catch (Exception e)
        {
        }
    }

}
