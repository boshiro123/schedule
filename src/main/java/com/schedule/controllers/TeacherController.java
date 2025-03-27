package com.schedule.controllers;

import com.schedule.models.*;
import com.schedule.service.ClassroomService;
import com.schedule.service.ScheduleService;
import com.schedule.service.StudentGroupService;
import com.schedule.service.SubjectService;
import com.schedule.service.UserService;
import com.schedule.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

  private final ScheduleService scheduleService;
  private final UserService userService;
  private final StudentGroupService studentGroupService;
  private final SubjectService subjectService;
  private final ClassroomService classroomService;
  private final SemesterService semesterService;

  public TeacherController(ScheduleService scheduleService, UserService userService,
      StudentGroupService studentGroupService, SubjectService subjectService,
      ClassroomService classroomService, SemesterService semesterService) {
    this.scheduleService = scheduleService;
    this.userService = userService;
    this.studentGroupService = studentGroupService;
    this.subjectService = subjectService;
    this.classroomService = classroomService;
    this.semesterService = semesterService;
  }

  @GetMapping("/schedule")
  public String getTeacherSchedule(Model model) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User teacher = userService.findUserByUsername(auth.getName()).orElse(null);

    if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
      return "redirect:/login";
    }

    // Получаем расписание преподавателя
    List<ScheduleEntry> scheduleEntries = scheduleService.findScheduleForTeacher(teacher);

    // Обрабатываем записи с null значениями в поле specificDate
    // Для регулярных занятий без конкретной даты устанавливаем текущую дату
    scheduleEntries.forEach(entry -> {
      if (entry.getSpecificDate() == null && entry.getIsRegular()) {
        // Для регулярных занятий используем текущую дату для отображения
        entry.setSpecificDate(LocalDate.now());
      }
    });

    model.addAttribute("teacher", teacher);
    model.addAttribute("scheduleEntries", scheduleEntries);

    return "teacher/schedule";
  }

  @GetMapping("/additional-class/new")
  public String newAdditionalClassForm(Model model) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User teacher = userService.findUserByUsername(auth.getName()).orElse(null);

    if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
      return "redirect:/login";
    }

    // Создаем новую запись для дополнительного занятия
    ScheduleEntry additionalClass = new ScheduleEntry();
    additionalClass.setTeacher(teacher);
    additionalClass.setLessonType(LessonType.ADDITIONAL);
    additionalClass.setIsRegular(false);

    // Установка текущего семестра
    Semester currentSemester = semesterService.findCurrentSemesterOrDefault();
    additionalClass.setSemester(currentSemester);

    model.addAttribute("additionalClass", additionalClass);
    model.addAttribute("groups", studentGroupService.findAllGroups());
    model.addAttribute("subjects", subjectService.findAllSubjects());
    model.addAttribute("classrooms", classroomService.findAllClassrooms());
    model.addAttribute("dayOfWeekValues", DayOfWeek.values());
    model.addAttribute("lessonTypeValues", LessonType.values());
    model.addAttribute("currentSemester", currentSemester);

    return "teacher/additional-class-form";
  }

  @PostMapping("/additional-class")
  public String saveAdditionalClass(@Valid @ModelAttribute("additionalClass") ScheduleEntry additionalClass,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User teacher = userService.findUserByUsername(auth.getName()).orElse(null);

    if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
      return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      model.addAttribute("classrooms", classroomService.findAllClassrooms());
      model.addAttribute("dayOfWeekValues", DayOfWeek.values());
      model.addAttribute("lessonTypeValues", LessonType.values());
      return "teacher/additional-class-form";
    }

    // Устанавливаем преподавателя и тип занятия
    additionalClass.setTeacher(teacher);
    additionalClass.setLessonType(LessonType.ADDITIONAL);
    additionalClass.setIsRegular(false);

    // Если семестр не установлен, устанавливаем текущий семестр
    if (additionalClass.getSemester() == null) {
      Semester currentSemester = semesterService.findCurrentSemesterOrDefault();
      additionalClass.setSemester(currentSemester);
    }

    // Установка дня недели на основе указанной даты
    if (additionalClass.getSpecificDate() != null && additionalClass.getDayOfWeek() == null) {
      additionalClass.setDayOfWeek(additionalClass.getSpecificDate().getDayOfWeek());
    }

    // Проверяем доступность аудитории
    boolean isClassroomAvailable = scheduleService.isClassroomAvailable(
        additionalClass.getClassroom(),
        additionalClass.getSpecificDate(),
        additionalClass.getStartTime(),
        additionalClass.getEndTime());

    if (!isClassroomAvailable) {
      model.addAttribute("classroomError", "Аудитория занята в указанное время");
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      model.addAttribute("classrooms", classroomService.findAllClassrooms());
      model.addAttribute("dayOfWeekValues", DayOfWeek.values());
      model.addAttribute("lessonTypeValues", LessonType.values());
      return "teacher/additional-class-form";
    }

    // Проверяем доступность группы
    boolean isGroupAvailable = scheduleService.isGroupAvailable(
        additionalClass.getGroup(),
        additionalClass.getSpecificDate(),
        additionalClass.getStartTime(),
        additionalClass.getEndTime());

    if (!isGroupAvailable) {
      model.addAttribute("groupError", "У группы уже есть занятие в указанное время");
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      model.addAttribute("classrooms", classroomService.findAllClassrooms());
      model.addAttribute("dayOfWeekValues", DayOfWeek.values());
      model.addAttribute("lessonTypeValues", LessonType.values());
      return "teacher/additional-class-form";
    }

    // Сохраняем дополнительное занятие
    scheduleService.saveScheduleEntry(additionalClass);

    redirectAttributes.addFlashAttribute("success", "Дополнительное занятие успешно добавлено");
    return "redirect:/teacher/schedule";
  }

  @GetMapping("/additional-class/edit/{id}")
  public String editAdditionalClassForm(@PathVariable Long id, Model model) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User teacher = userService.findUserByUsername(auth.getName()).orElse(null);

    if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
      return "redirect:/login";
    }

    Optional<ScheduleEntry> additionalClassOpt = scheduleService.findScheduleEntryById(id);
    if (additionalClassOpt.isPresent()) {
      ScheduleEntry additionalClass = additionalClassOpt.get();

      // Проверяем, что это занятие принадлежит текущему преподавателю
      if (!additionalClass.getTeacher().getId().equals(teacher.getId())) {
        return "redirect:/teacher/schedule";
      }

      // Проверяем, что это дополнительное занятие
      if (additionalClass.getLessonType() != LessonType.ADDITIONAL) {
        return "redirect:/teacher/schedule";
      }

      // Если семестр не установлен, устанавливаем текущий семестр
      if (additionalClass.getSemester() == null) {
        Semester currentSemester = semesterService.findCurrentSemesterOrDefault();
        additionalClass.setSemester(currentSemester);
      }

      model.addAttribute("additionalClass", additionalClass);
      model.addAttribute("groups", studentGroupService.findAllGroups());
      model.addAttribute("subjects", subjectService.findAllSubjects());
      model.addAttribute("classrooms", classroomService.findAllClassrooms());
      model.addAttribute("dayOfWeekValues", DayOfWeek.values());
      model.addAttribute("lessonTypeValues", LessonType.values());
      model.addAttribute("currentSemester", additionalClass.getSemester());

      return "teacher/additional-class-form";
    }

    return "redirect:/teacher/schedule";
  }

  @GetMapping("/additional-class/delete/{id}")
  public String deleteAdditionalClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    // Получаем текущего пользователя
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User teacher = userService.findUserByUsername(auth.getName()).orElse(null);

    if (teacher == null || teacher.getRole() != UserRole.TEACHER) {
      return "redirect:/login";
    }

    Optional<ScheduleEntry> additionalClassOpt = scheduleService.findScheduleEntryById(id);
    if (additionalClassOpt.isPresent()) {
      ScheduleEntry additionalClass = additionalClassOpt.get();

      // Проверяем, что это занятие принадлежит текущему преподавателю
      if (!additionalClass.getTeacher().getId().equals(teacher.getId())) {
        return "redirect:/teacher/schedule";
      }

      // Проверяем, что это дополнительное занятие
      if (additionalClass.getLessonType() != LessonType.ADDITIONAL) {
        return "redirect:/teacher/schedule";
      }

      scheduleService.deleteScheduleEntry(id);
      redirectAttributes.addFlashAttribute("success", "Дополнительное занятие успешно удалено");
    }

    return "redirect:/teacher/schedule";
  }
}