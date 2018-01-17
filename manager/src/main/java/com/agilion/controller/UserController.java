package com.agilion.controller;

import com.agilion.domain.app.User;
import com.agilion.services.app.UserService;
import com.agilion.services.dao.UserRepository;
import com.agilion.services.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

/**
 * Created by Alex_Lappy_486 on 1/30/17.
 */
@Controller
public class UserController
{
    @Autowired
    UserValidator userValidator;

    @Autowired
    UserService userService;

    @InitBinder("createdUser")
    public void setupBinder(WebDataBinder binder)
    {
        binder.addValidators(userValidator);
    }

    @RequestMapping("/register")
    public String register(Model model)
    {
        model.addAttribute("createdUser", new User());
        return "register";
    }

    @RequestMapping(value = "/registerUser", method = RequestMethod.POST)
    public String attemptUserRegistration(Model model, @ModelAttribute("createdUser")
        @Validated(User.UserRegistration.class) User createdUser,
        BindingResult bindingResult)
    {
        if (bindingResult.hasErrors())
        {
            return "register";
        }
        else
        {
            // Save new user and tell the login page we just registered
            this.userService.registerNewUser(createdUser);

            model.addAttribute("user", new User());
            model.addAttribute("createdUserAccount", true);
            return "redirect:/login";
        }
    }

    @RequestMapping("/login")
    public String login()
    {
        return "login";
    }
}
