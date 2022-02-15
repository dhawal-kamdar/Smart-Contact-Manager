package com.smart.smartcontactmanager.controller;

import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        // Get current user's username(email)
        String userName = principal.getName();
        System.out.println("USERNAME: " + userName);

        // Get user by username
        User user = userRepository.getUserByUsername(userName);
        System.out.println("USER DETAILS: " + user.toString());

        model.addAttribute("user", user);
    }

    @GetMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "/normal/user-dashboard";
    }

    @GetMapping("/add-contact")
    public String addContact(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add-contact";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session) {
        try {
            String username = principal.getName();
            User user = this.userRepository.getUserByUsername(username);

            // Profile Image Processing
            if(file.isEmpty()) {
                System.out.println("IMAGE FILE IS EMPTY - NOT UPLOADED");
            } else {
                contact.setImageUrl(contact.getPhone() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                File folder = new ClassPathResource("static/images").getFile();
                Path targetPath = Paths.get(folder.getAbsolutePath() + File.separator + contact.getPhone() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("IMAGE IS UPLOADED");
            }

            contact.setUser(user);
            user.getContacts().add(contact);

            this.userRepository.save(user);

            session.setAttribute("message", new Message("Contact added successfully", "success"));

            System.out.println("CONTACT ADDED TO DATABASE");
        } catch (Exception e) {
            session.setAttribute("message", new Message("Something went wrong, Try again", "danger"));

            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return "normal/add-contact";
    }
    
}
