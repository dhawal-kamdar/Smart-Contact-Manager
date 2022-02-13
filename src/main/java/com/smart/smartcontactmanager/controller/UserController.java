package com.smart.smartcontactmanager.controller;

import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/index")
    public String dashboard(Model model, Principal principal) {

        // Get current user's username(email)
        String userName = principal.getName();
        System.out.println("USERNAME: " + userName);

        // Get user by username
        User user = userRepository.getUserByUsername(userName);
        System.out.println("USER DETAILS: " + user.toString());

        model.addAttribute("user", user);

        return "/normal/user-dashboard";
    }

}
