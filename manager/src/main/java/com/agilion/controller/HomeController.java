package com.agilion.controller;

import com.agilion.domain.app.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(user.getSubmittedNetworkBuildJobIds());
        return "home";
    }
}
