package com.agilion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Alex_Lappy_486 on 3/7/17.
 */
@Controller
public class React
{
    @RequestMapping("/react")
    public String reactDemo()
    {
        return "session/reactExample";
    }
}
