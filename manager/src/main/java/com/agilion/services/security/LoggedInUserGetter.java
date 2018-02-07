package com.agilion.services.security;

import com.agilion.domain.app.User;

public interface LoggedInUserGetter
{
    public User getCurrentlyLoggedInUser() throws NoLoggedInUserException;
}
