package com.schedule.controllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/utils")
public class PasswordEncoderController {

  private final PasswordEncoder passwordEncoder;

  public PasswordEncoderController(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/password-encoder")
  public String showPasswordEncoderPage() {
    return "utils/password-encoder";
  }

  @PostMapping("/password-encoder")
  public String encodePassword(@RequestParam String password, Model model) {
    String encodedPassword = passwordEncoder.encode(password);
    model.addAttribute("password", password);
    model.addAttribute("encodedPassword", encodedPassword);
    return "utils/password-encoder";
  }
}