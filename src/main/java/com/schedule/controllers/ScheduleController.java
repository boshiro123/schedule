package com.schedule.controllers;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.service.ScheduleService;
import com.schedule.service.StudentGroupService;
import com.schedule.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

  private final ScheduleService scheduleService;
  private final StudentGroupService studentGroupService;
  private final UserService userService;

  public ScheduleController(ScheduleService scheduleService, StudentGroupService studentGroupService,
      UserService userService) {
    this.scheduleService = scheduleService;
    this.studentGroupService = studentGroupService;
    this.userService = userService;
  }

  // Публичное расписание для группы (доступно без авторизации)
  @GetMapping("/public/group/{groupId}")
  public String getGroupSchedule(@PathVariable Long groupId, Model model) {
    Optional<StudentGroup> groupOpt = studentGroupService.findGroupById(groupId);
    if (groupOpt.isPresent()) {
      StudentGroup group = groupOpt.get();
      List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForGroup(group);

      model.addAttribute("group", group);
      model.addAttribute("scheduleEntries", scheduleEntries);
      return "schedule/group-schedule";
    }
    return "error/404";
  }

  // Личное расписание для авторизованного пользователя
  @GetMapping("/personal")
  public String getPersonalSchedule(Model model) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      User user = userService.findUserByUsername(auth.getName()).orElse(null);
      if (user != null) {
        List<ScheduleEntry> scheduleEntries;

        // В зависимости от роли пользователя показываем разное расписание
        if (user.getRole().name().equals("TEACHER")) {
          scheduleEntries = scheduleService.findScheduleForTeacher(user);
          model.addAttribute("isTeacher", true);
        } else if (user.getGroup() != null) {
          scheduleEntries = scheduleService.findScheduleForGroup(user.getGroup());
          model.addAttribute("group", user.getGroup());
        } else {
          return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("scheduleEntries", scheduleEntries);
        return "schedule/personal-schedule";
      }
    }
    return "redirect:/login";
  }
}