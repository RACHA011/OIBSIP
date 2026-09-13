package com.oibsip.library.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.oibsip.library.model.User;
import com.oibsip.library.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;

    AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";
        return currentUser.getRole() == User.Role.ADMIN ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         HttpSession session, Model model) {
        Optional<User> user = userService.authenticate(email, password);
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
        session.setAttribute("currentUser", user.get());
        return user.get().getRole() == User.Role.ADMIN ? "redirect:/admin/dashboard" : "redirect:/user/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName, @RequestParam String email,
                            @RequestParam String password, Model model) {
        if (userService.emailExists(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        userService.register(fullName, email, password);
        model.addAttribute("success", "Account created. Please log in.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}