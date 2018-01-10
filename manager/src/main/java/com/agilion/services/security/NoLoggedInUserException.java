package com.agilion.services.security;

public class NoLoggedInUserException extends Exception
{
    public NoLoggedInUserException()
    {
        super("There is no currently-logged-in user!");
    }
}
