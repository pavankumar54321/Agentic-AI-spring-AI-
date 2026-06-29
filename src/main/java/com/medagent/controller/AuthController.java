package com.medagent.controller;

import com.medagent.domain.PatientProfile;
import com.medagent.repository.PatientProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class AuthController {

    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PatientProfileRepository patientProfileRepository, PasswordEncoder passwordEncoder) {
        this.patientProfileRepository = patientProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new PatientProfile());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute PatientProfile user, Model model) {
        Optional<PatientProfile> existing = patientProfileRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            model.addAttribute("error", "Email is already registered!");
            return "register";
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        patientProfileRepository.save(user);
        
        return "redirect:/login?registered";
    }
}
