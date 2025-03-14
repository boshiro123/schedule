package com.schedule.controllers;

import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.service.StudentGroupService;
import com.schedule.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

  private final UserService userService;
  private final StudentGroupService studentGroupService;

  public HomeController(UserService userService, StudentGroupService studentGroupService) {
    this.userService = userService;
    this.studentGroupService = studentGroupService;
  }

  @GetMapping("/")
  public String home(Model model) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
      User user = userService.findUserByUsername(auth.getName()).orElse(null);
      if (user != null) {
        model.addAttribute("user", user);

        // Перенаправляем на соответствующую страницу в зависимости от роли
        if (user.getRole() == UserRole.DISPATCHER) {
          return "redirect:/admin/dashboard";
        } else if (user.getRole() == UserRole.TEACHER) {
          return "redirect:/teacher/schedule";
        }
      }
    }

    // Для неавторизованных пользователей или студентов показываем список групп
    List<StudentGroup> groups = studentGroupService.findAllGroups();
    model.addAttribute("groups", groups);

    return "home";
  }
}