package com.agilion.controller;

import com.agilion.domain.app.User;
import com.agilion.services.dataengine.DataEngineClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 2/1/17.
 */
@Controller
public class HomeController
{
    @Autowired
    DataEngineClient client;

    @RequestMapping(value = {"/", "/home"})
    public String initHome()
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "home";
    }
}
