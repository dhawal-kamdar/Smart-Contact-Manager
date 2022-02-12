package com.smart.smartcontactmanager.controller;

import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register-user")
    public String registerUser(@ModelAttribute("user") User user, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
        try {
            System.out.println("USER: " + user.toString());
            System.out.println("USER agreement: " + agreement);

            if(!agreement) {
                System.out.println("You have not agreed to terms and conditions");
                throw new Exception("You have not agreed to terms and conditions");
            }

            user.setImageUrl("default.png");
            user.setRole("ROLE_USER");
            user.setEnabled(true);

            User savedUser = userRepository.save(user);

            model.addAttribute("user", new User());

            session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong - " + e.getMessage(), "alert-danger"));
        }

        return "signup";
    }
}
