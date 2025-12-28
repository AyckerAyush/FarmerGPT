package com.farmer.ai.controller;

import com.farmer.ai.model.User;
import com.farmer.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user, HttpServletRequest request) {
        userService.registerUser(user);
        // Auto Login
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                java.util.Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        authToken.setDetails(new WebAuthenticationDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return "redirect:/chat";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(String email, String securityQuestion, String securityAnswer, Model model) {
        User user = userService.findByEmail(email);

        if (user != null &&
                user.getSecurityQuestion() != null &&
                user.getSecurityQuestion().equals(securityQuestion) &&
                userService.checkSecurityAnswer(user, securityAnswer)) {

            String token = java.util.UUID.randomUUID().toString();
            userService.updateResetToken(token, email);
            model.addAttribute("token", token);
            return "reset_password";
        } else {
            model.addAttribute("error", "Invalid email, security question, or answer.");
            return "forgot_password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(String token, Model model) {
        User user = userService.getByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid Token");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(String token, String password, Model model) {
        User user = userService.getByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid Token");
            return "redirect:/login";
        } else {
            userService.updatePassword(user, password);
            model.addAttribute("message", "You have successfully changed your password.");
            return "login";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, java.security.Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(String name, String securityQuestion, String securityAnswer, String password,
            Model model, java.security.Principal principal) {
        String email = principal.getName();
        User user = userService.findByEmail(email);
        user.setName(name);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer);
        if (password != null && !password.isEmpty()) {
            userService.updatePassword(user, password);
        } else {
            userService.updateUser(user);
        }
        model.addAttribute("user", user);
        model.addAttribute("message", "Profile updated successfully.");
        return "profile";
    }

}
