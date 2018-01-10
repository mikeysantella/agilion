package com.agilion.config.demo;

import com.agilion.domain.app.User;
import com.agilion.services.app.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DemoConfig
{
    @Autowired
    UserService userService;

    @Autowired
    DemoSecurity demoSecurity;

    @PostConstruct
    public void afterPropertiesSet()
    {
        if (this.demoSecurity.isEnabled())
        {
            User defaultDemoUser = new User();
            defaultDemoUser.setUsername(this.demoSecurity.getDefaultUser());
            defaultDemoUser.setPassword(this.demoSecurity.getDefaultPassword());
            defaultDemoUser.setConfirmPassword(this.demoSecurity.getDefaultPassword());
            defaultDemoUser.setEmail("admin@admin.com");
            userService.registerNewUser(defaultDemoUser);
        }
    }
}
