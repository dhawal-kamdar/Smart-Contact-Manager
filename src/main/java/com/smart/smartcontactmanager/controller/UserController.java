package com.smart.smartcontactmanager.controller;

import com.smart.smartcontactmanager.dao.ContactRepository;
import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

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
                contact.setImageUrl("default.png");
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

    @GetMapping("/view-contacts/{page}")
    public String viewContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "View Contacts");

        String username = principal.getName();
        User user = this.userRepository.getUserByUsername(username);

        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/view-contacts";
    }

    @GetMapping("/contact/{cId}")
    public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        model.addAttribute("title", "Contact Details");

        // Check if user have access to the contact
        String username = principal.getName();
        User user = this.userRepository.getUserByUsername(username);

        if(user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
        }

        return "normal/contact-details";
    }

    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) throws IOException {
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        // Check if user have access to the contact
        String username = principal.getName();
        User user = this.userRepository.getUserByUsername(username);

        if(user.getId() == contact.getUser().getId()) {
            // Delete Profile Picture
            if(!contact.getImageUrl().equals("default.png")) {
                File folder = new ClassPathResource("static/images").getFile();
                Path targetPath = Paths.get(folder.getAbsolutePath() + File.separator + contact.getImageUrl());
                Files.delete(targetPath);
                System.out.println("Deleted Profile Picture:" + contact.getImageUrl());
            }

            // Delete Contact from Database
            contact.setUser(null);
            user.getContacts().removeIf(c -> c.getcId() == contact.getcId());
            this.contactRepository.delete(contact);

            session.setAttribute("message", new Message("Contact deleted successfully", "success"));
        } else {
            session.setAttribute("message", new Message("You don't have access to this contact", "danger"));
        }

        return "redirect:/user/view-contacts/0";
    }

    @PostMapping("/update/{cId}")
    public String updateContact(@PathVariable("cId") Integer cId, Model model) {
        model.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cId).get();
        model.addAttribute("contact", contact);
        return "normal/update-contact";
    }

    @PostMapping("/process-update")
    public String updateContact(@ModelAttribute Contact contact,
                                @RequestParam("profileImage") MultipartFile file,
                                Model model,
                                Principal principal,
                                HttpSession session) {
        try {
            Contact oldContact = this.contactRepository.findById(contact.getcId()).get();

            if(!file.isEmpty()) {
                File folder = new ClassPathResource("static/images").getFile();

                // Delete previous image
                Path deletePath = Paths.get(folder.getAbsolutePath() + File.separator + oldContact.getImageUrl());
                Files.delete(deletePath);

                // Update new image
                Path updatePath = Paths.get(folder.getAbsolutePath() + File.separator + contact.getPhone() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                Files.copy(file.getInputStream(), updatePath, StandardCopyOption.REPLACE_EXISTING);
                contact.setImageUrl(contact.getPhone() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
            } else {
                contact.setImageUrl(oldContact.getImageUrl());
            }

            String username = principal.getName();
            User user = this.userRepository.getUserByUsername(username);
            contact.setUser(user);
            this.contactRepository.save(contact);

            session.setAttribute("message", new Message("Contact updated successfully", "success"));
        } catch (Exception e) {
            session.setAttribute("message", new Message("Contact update failed", "danger"));
            e.printStackTrace();
        }
        
        return "redirect:/user/contact/" + contact.getcId();
    }

}
