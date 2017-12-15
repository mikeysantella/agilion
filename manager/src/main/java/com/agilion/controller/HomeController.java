package com.agilion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 2/1/17.
 */
@RequestMapping("/home")
@Controller
public class HomeController
{
    @RequestMapping
    public String initHome()
    {
        return "home";
    }
}
