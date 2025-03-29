package com.schedule.controllers;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.service.ScheduleService;
import com.schedule.service.StudentGroupService;
import com.schedule.service.UserService;
import com.schedule.service.export.ScheduleExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

  private final ScheduleService scheduleService;
  private final StudentGroupService studentGroupService;
  private final UserService userService;
  private final ScheduleExportService scheduleExportService;

  public ScheduleController(ScheduleService scheduleService, StudentGroupService studentGroupService,
      UserService userService, ScheduleExportService scheduleExportService) {
    this.scheduleService = scheduleService;
    this.studentGroupService = studentGroupService;
    this.userService = userService;
    this.scheduleExportService = scheduleExportService;
  }

  // Публичное расписание для группы (доступно без авторизации)
  @GetMapping("/public/group/{groupId}")
  public String getGroupSchedule(@PathVariable Long groupId, Model model) {
    Optional<StudentGroup> groupOpt = studentGroupService.findGroupById(groupId);
    if (groupOpt.isPresent()) {
      StudentGroup group = groupOpt.get();
      List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForGroup(group);

      // Обработка записей с null значениями в поле dayOfWeek
      scheduleEntries.forEach(entry -> {
        if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
          // Если день недели не указан, но дата указана, определяем день недели из даты
          entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
          System.out.println("Day of week: " + entry.getDayOfWeek());
        }
      });

      model.addAttribute("group", group);
      model.addAttribute("scheduleEntries", scheduleEntries);
      return "schedule/group-schedule";
    }
    return "error/404";
  }

  // Публичное расписание для преподавателя (доступно без авторизации)
  @GetMapping("/public/teacher/{teacherId}")
  public String getTeacherSchedule(@PathVariable Long teacherId, Model model) {
    Optional<User> teacherOpt = userService.findUserById(teacherId);
    if (teacherOpt.isPresent() && teacherOpt.get().getRole() == UserRole.TEACHER) {
      User teacher = teacherOpt.get();
      List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForTeacher(teacher);

      // Обработка записей с null значениями в поле dayOfWeek
      scheduleEntries.forEach(entry -> {
        if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
          // Если день недели не указан, но дата указана, определяем день недели из даты
          entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
          System.out.println("Day of week: " + entry.getDayOfWeek());
        }
      });

      model.addAttribute("teacher", teacher);
      model.addAttribute("scheduleEntries", scheduleEntries);
      return "schedule/teacher-schedule";
    }
    return "error/404";
  }

  // Список всех доступных расписаний (публичная страница)
  @GetMapping("/public")
  public String getPublicScheduleList(Model model) {
    List<StudentGroup> groups = studentGroupService.findAllGroups();
    groups.sort(Comparator.comparing(StudentGroup::getName));

    List<User> teachers = userService.findAllUsers().stream()
        .filter(user -> user.getRole() == UserRole.TEACHER)
        .sorted(Comparator.comparing(User::getFullName))
        .collect(Collectors.toList());

    model.addAttribute("groups", groups);
    model.addAttribute("teachers", teachers);
    return "schedule/public-list";
  }

  // Личное расписание для авторизованного пользователя
  @GetMapping("/personal")
  public String getPersonalSchedule(Model model, RedirectAttributes redirectAttributes) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    Optional<User> userOpt = userService.findUserByUsername(username);
    if (!userOpt.isPresent()) {
      redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
      return "redirect:/";
    }

    User user = userOpt.get();

    // Студенту показываем расписание его группы
    if (user.getRole() == UserRole.STUDENT && user.getGroup() != null) {
      StudentGroup group = user.getGroup();
      List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForGroup(group);

      // Обработка записей с null значениями в поле dayOfWeek
      scheduleEntries.forEach(entry -> {
        if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
          entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
          System.out.println("Day of week: " + entry.getDayOfWeek());
        }
      });

      model.addAttribute("group", group);
      model.addAttribute("scheduleEntries", scheduleEntries);
      return "schedule/group-schedule";
    }

    // Преподавателю показываем его личное расписание
    if (user.getRole() == UserRole.TEACHER) {
      List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForTeacher(user);

      // Обработка записей с null значениями в поле dayOfWeek
      scheduleEntries.forEach(entry -> {
        if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
          entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
        }
      });

      model.addAttribute("teacher", user);
      model.addAttribute("scheduleEntries", scheduleEntries);
      return "schedule/teacher-schedule";
    }

    redirectAttributes.addFlashAttribute("error", "У вас нет доступа к расписанию");
    return "redirect:/";
  }

  /**
   * Экспорт расписания группы в Excel
   */
  @GetMapping("/public/group/{groupId}/export")
  public ResponseEntity<byte[]> exportGroupSchedule(@PathVariable Long groupId) {
    Optional<StudentGroup> groupOpt = studentGroupService.findGroupById(groupId);
    if (groupOpt.isPresent()) {
      StudentGroup group = groupOpt.get();
      try {
        byte[] excelContent = scheduleExportService.exportGroupScheduleToExcel(group);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"schedule_group_" + group.getName() + ".xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelContent);
      } catch (IOException e) {
        return ResponseEntity.internalServerError().build();
      }
    }
    return ResponseEntity.notFound().build();
  }

  /**
   * Экспорт расписания преподавателя в Excel
   */
  @GetMapping("/public/teacher/{teacherId}/export")
  public ResponseEntity<byte[]> exportTeacherSchedule(@PathVariable Long teacherId) {
    Optional<User> teacherOpt = userService.findUserById(teacherId);
    if (teacherOpt.isPresent() && teacherOpt.get().getRole() == UserRole.TEACHER) {
      User teacher = teacherOpt.get();
      try {
        byte[] excelContent = scheduleExportService.exportTeacherScheduleToExcel(teacher);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"schedule_teacher" + ".xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelContent);
      } catch (IOException e) {
        return ResponseEntity.internalServerError().build();
      }
    }
    return ResponseEntity.notFound().build();
  }

  /**
   * Экспорт личного расписания в Excel
   */
  @GetMapping("/personal/export")
  public ResponseEntity<byte[]> exportPersonalSchedule() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    Optional<User> userOpt = userService.findUserByUsername(username);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    User user = userOpt.get();

    try {
      byte[] excelContent;
      String filename;

      // Студенту экспортируем расписание его группы
      if (user.getRole() == UserRole.STUDENT && user.getGroup() != null) {
        StudentGroup group = user.getGroup();
        excelContent = scheduleExportService.exportGroupScheduleToExcel(group);
        filename = "schedule_group_" + group.getName() + ".xlsx";
      }
      // Преподавателю экспортируем его личное расписание
      else if (user.getRole() == UserRole.TEACHER) {
        excelContent = scheduleExportService.exportTeacherScheduleToExcel(user);
        filename = "schedule_teacher_" + user.getFullName() + ".xlsx";
      }
      // Диспетчеру - полное расписание
      else if (user.getRole() == UserRole.DISPATCHER) {
        excelContent = scheduleExportService.exportFullScheduleToExcel();
        filename = "schedule_full.xlsx";
      } else {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(excelContent);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Экспорт полного расписания в Excel (для диспетчера)
   */
  @GetMapping("/export/full")
  public ResponseEntity<byte[]> exportFullSchedule() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

    Optional<User> userOpt = userService.findUserByUsername(username);
    if (!userOpt.isPresent() || userOpt.get().getRole() != UserRole.DISPATCHER) {
      return ResponseEntity.status(403).build(); // Forbidden
    }

    try {
      byte[] excelContent = scheduleExportService.exportFullScheduleToExcel();
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schedule_full.xlsx\"")
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(excelContent);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}