package com.agilion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 2/5/17.
 */
@Controller
@RequestMapping("/session/new")
public class NewSessionController
{
    @RequestMapping
    public String initSessionHistoryPage()
    {
        return "session/newSession";
    }
}
