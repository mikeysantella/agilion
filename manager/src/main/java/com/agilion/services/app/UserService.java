package com.agilion.services.app;

import com.agilion.domain.app.Role;
import com.agilion.domain.app.User;
import com.agilion.services.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService implements UserDetailsService
{
    /**
     * When registering new users, give them the least amount of access possible. The admin is then responsible for giving
     * them elevated privalges (i.e. upgrade to NORMAL_USER or ADMIN)
     */
    private static final Role DEFAULT_NEW_USER_ROLE = Role.BROWSE;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void registerNewUser(User user)
    {
        String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        user.setRole(DEFAULT_NEW_USER_ROLE);
        user.setDateCreated(new Date());
        this.userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  this.userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("USERNAME NOT FOUND, YOU IDIOT");

        return user;
    }
}
