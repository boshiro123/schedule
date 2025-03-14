package com.schedule.controllers;

import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/login")
  public String login() {
    // Если пользователь уже авторизован, перенаправляем на главную страницу
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      return "redirect:/";
    }
    return "auth/login";
  }

  @GetMapping("/change-password")
  public String changePasswordForm(Model model) {
    model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
    return "auth/change-password";
  }

  @PostMapping("/change-password")
  public String changePassword(@Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "auth/change-password";
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    User user = userService.findUserByUsername(username).orElse(null);

    if (user != null && userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword())) {
      model.addAttribute("success", true);
      return "auth/change-password-success";
    } else {
      model.addAttribute("error", "Неверный текущий пароль");
      return "auth/change-password";
    }
  }

  @GetMapping("/logout")
  public String logout(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, null, auth);
    }
    return "redirect:/login?logout";
  }

  // Вспомогательный класс для изменения пароля
  public static class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public String getOldPassword() {
      return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
      this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
      return newPassword;
    }

    public void setNewPassword(String newPassword) {
      this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
      return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
      this.confirmPassword = confirmPassword;
    }
  }
}