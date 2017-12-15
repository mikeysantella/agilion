package com.agilion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 1/30/17.
 */
@Controller
public class LoginController
{
    @RequestMapping("/login")
    public String login()
    {
        return "login";
    }
}
