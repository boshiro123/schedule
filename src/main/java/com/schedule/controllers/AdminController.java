package com.schedule.controllers;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.service.ScheduleService;
import com.schedule.service.StudentGroupService;
import com.schedule.service.SubjectService;
import com.schedule.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final UserService userService;
  private final StudentGroupService studentGroupService;
  private final SubjectService subjectService;
  private final ScheduleService scheduleService;

  public AdminController(UserService userService, StudentGroupService studentGroupService,
      SubjectService subjectService, ScheduleService scheduleService) {
    this.userService = userService;
    this.studentGroupService = studentGroupService;
    this.subjectService = subjectService;
    this.scheduleService = scheduleService;
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    long teachersCount = userService.findAllUsers().stream()
        .filter(u -> u.getRole() == UserRole.TEACHER).count();
    long groupsCount = studentGroupService.findAllGroups().size();
    long subjectsCount = subjectService.findAllSubjects().size();
    long scheduleEntriesCount = scheduleService.findAllScheduleEntries().size();

    model.addAttribute("teachersCount", teachersCount);
    model.addAttribute("groupsCount", groupsCount);
    model.addAttribute("subjectsCount", subjectsCount);
    model.addAttribute("scheduleEntriesCount", scheduleEntriesCount);

    return "admin/dashboard";
  }

  // Управление преподавателями
  @GetMapping("/teachers")
  public String listTeachers(Model model) {
    List<User> teachers = userService.findAllUsers().stream()
        .filter(u -> u.getRole() == UserRole.TEACHER)
        .toList();
    model.addAttribute("teachers", teachers);
    return "admin/teachers/list";
  }

  @GetMapping("/teachers/new")
  public String newTeacherForm(Model model) {
    model.addAttribute("teacher", new User());
    return "admin/teachers/form";
  }

  @PostMapping("/teachers")
  public String saveTeacher(@Valid @ModelAttribute("teacher") User teacher,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "admin/teachers/form";
    }

    teacher.setRole(UserRole.TEACHER);
    userService.createUser(teacher);
    redirectAttributes.addFlashAttribute("success", "Преподаватель успешно добавлен");
    return "redirect:/admin/teachers";
  }

  @GetMapping("/teachers/edit/{id}")
  public String editTeacherForm(@PathVariable Long id, Model model) {
    Optional<User> teacherOpt = userService.findUserById(id);
    if (teacherOpt.isPresent() && teacherOpt.get().getRole() == UserRole.TEACHER) {
      model.addAttribute("teacher", teacherOpt.get());
      return "admin/teachers/form";
    }
    return "redirect:/admin/teachers";
  }

  @GetMapping("/teachers/delete/{id}")
  public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    Optional<User> teacherOpt = userService.findUserById(id);
    if (teacherOpt.isPresent() && teacherOpt.get().getRole() == UserRole.TEACHER) {
      userService.deleteUser(id);
      redirectAttributes.addFlashAttribute("success", "Преподаватель успешно удален");
    }
    return "redirect:/admin/teachers";
  }

  // Управление группами
  @GetMapping("/groups")
  public String listGroups(Model model) {
    List<StudentGroup> groups = studentGroupService.findAllGroups();
    model.addAttribute("groups", groups);
    return "admin/groups/list";
  }

  @GetMapping("/groups/new")
  public String newGroupForm(Model model) {
    model.addAttribute("group", new StudentGroup());
    return "admin/groups/form";
  }

  @PostMapping("/groups")
  public String saveGroup(@Valid @ModelAttribute("group") StudentGroup group,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "admin/groups/form";
    }

    studentGroupService.saveGroup(group);
    redirectAttributes.addFlashAttribute("success", "Группа успешно добавлена");
    return "redirect:/admin/groups";
  }

  @GetMapping("/groups/edit/{id}")
  public String editGroupForm(@PathVariable Long id, Model model) {
    Optional<StudentGroup> groupOpt = studentGroupService.findGroupById(id);
    if (groupOpt.isPresent()) {
      model.addAttribute("group", groupOpt.get());
      return "admin/groups/form";
    }
    return "redirect:/admin/groups";
  }

  @GetMapping("/groups/delete/{id}")
  public String deleteGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    studentGroupService.deleteGroup(id);
    redirectAttributes.addFlashAttribute("success", "Группа успешно удалена");
    return "redirect:/admin/groups";
  }

  // Управление расписанием
  @GetMapping("/schedule")
  public String listSchedule(Model model) {
    List<ScheduleEntry> scheduleEntries = scheduleService.findAllScheduleEntries();
    model.addAttribute("scheduleEntries", scheduleEntries);
    return "admin/schedule/list";
  }

  @GetMapping("/schedule/new")
  public String newScheduleEntryForm(Model model) {
    model.addAttribute("scheduleEntry", new ScheduleEntry());
    model.addAttribute("teachers", userService.findAllUsers().stream()
        .filter(u -> u.getRole() == UserRole.TEACHER)
        .toList());
    model.addAttribute("groups", studentGroupService.findAllGroups());
    model.addAttribute("subjects", subjectService.findAllSubjects());
    return "admin/schedule/form";
  }

  @PostMapping("/schedule")
  public String saveScheduleEntry(@Valid @ModelAttribute("scheduleEntry") ScheduleEntry scheduleEntry,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("teachers", userService.findAllUsers().stream()
          .filter(u -> u.getRole() == UserRole.TEACHER)
          .toList());
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      return "admin/schedule/form";
    }

    scheduleService.saveScheduleEntry(scheduleEntry);
    redirectAttributes.addFlashAttribute("success", "Запись в расписании успешно добавлена");
    return "redirect:/admin/schedule";
  }

  @GetMapping("/schedule/edit/{id}")
  public String editScheduleEntryForm(@PathVariable Long id, Model model) {
    Optional<ScheduleEntry> scheduleEntryOpt = scheduleService.findScheduleEntryById(id);
    if (scheduleEntryOpt.isPresent()) {
      model.addAttribute("scheduleEntry", scheduleEntryOpt.get());
      model.addAttribute("teachers", userService.findAllUsers().stream()
          .filter(u -> u.getRole() == UserRole.TEACHER)
          .toList());
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      return "admin/schedule/form";
    }
    return "redirect:/admin/schedule";
  }

  @GetMapping("/schedule/delete/{id}")
  public String deleteScheduleEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    scheduleService.deleteScheduleEntry(id);
    redirectAttributes.addFlashAttribute("success", "Запись в расписании успешно удалена");
    return "redirect:/admin/schedule";
  }
}