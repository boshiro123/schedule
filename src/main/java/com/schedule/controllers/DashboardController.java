package com.schedule.controllers;

import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

  private final UserService userService;

  public DashboardController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/dashboard")
  public String dashboard() {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findUserByUsername(auth.getName()).orElse(null);

    if (user == null) {
      return "redirect:/login";
    }

    // Перенаправляем пользователя в зависимости от его роли
    if (user.getRole() == UserRole.DISPATCHER) {
      return "redirect:/admin/dashboard";
    } else if (user.getRole() == UserRole.TEACHER) {
      return "redirect:/teacher/schedule";
    } else if (user.getRole() == UserRole.STUDENT) {
      return "redirect:/schedule/personal";
    }

    // По умолчанию перенаправляем на главную страницу
    return "redirect:/";
  }
}