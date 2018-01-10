package com.agilion.services.security;

import com.agilion.domain.app.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityUserGetter implements LoggedInUserGetter
{
    @Override
    public User getCurrentlyLoggedInUser() throws NoLoggedInUserException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            throw new NoLoggedInUserException();

        return user;
    }
}
