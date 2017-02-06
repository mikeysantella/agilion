package com.agilion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 2/3/17.
 */
@Controller
@RequestMapping("/session/history")
public class SessionHistoryController
{
    @RequestMapping
    public String initSessionHistoryPage()
    {
        return "session/sessionHistory";
    }
}
