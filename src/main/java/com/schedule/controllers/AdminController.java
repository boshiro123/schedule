package com.schedule.controllers;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.Subject;
import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.models.LessonType;
import com.schedule.models.Classroom;
import com.schedule.models.Semester;
import com.schedule.service.ScheduleService;
import com.schedule.service.StudentGroupService;
import com.schedule.service.SubjectService;
import com.schedule.service.UserService;
import com.schedule.service.ClassroomService;
import com.schedule.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Objects;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final UserService userService;
  private final StudentGroupService studentGroupService;
  private final SubjectService subjectService;
  private final ScheduleService scheduleService;
  private final ClassroomService classroomService;
  private final SemesterService semesterService;

  public AdminController(UserService userService, StudentGroupService studentGroupService,
      SubjectService subjectService, ScheduleService scheduleService,
      ClassroomService classroomService, SemesterService semesterService) {
    this.userService = userService;
    this.studentGroupService = studentGroupService;
    this.subjectService = subjectService;
    this.scheduleService = scheduleService;
    this.classroomService = classroomService;
    this.semesterService = semesterService;
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
      RedirectAttributes redirectAttributes,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/teachers/form";
    }

    teacher.setRole(UserRole.TEACHER);

    // Проверяем, новый преподаватель или существующий
    if (teacher.getId() != null) {
      // Если редактируем существующего преподавателя
      Optional<User> existingTeacher = userService.findUserById(teacher.getId());
      if (existingTeacher.isPresent()) {
        // Если пароль пустой, используем существующий пароль
        if (teacher.getPassword() == null || teacher.getPassword().isEmpty()) {
          teacher.setPassword(existingTeacher.get().getPassword());
          userService.updateUser(teacher);
        } else {
          // Если указан новый пароль, обновляем с шифрованием
          userService.updateUser(teacher);
        }
        redirectAttributes.addFlashAttribute("success", "Преподаватель успешно обновлен");
      } else {
        redirectAttributes.addFlashAttribute("error", "Преподаватель не найден");
      }
    } else {
      // Если добавляем нового преподавателя, проверяем уникальность логина
      if (userService.existsByUsername(teacher.getUsername())) {
        model.addAttribute("usernameError", "Пользователь с таким логином уже существует");
        return "admin/teachers/form";
      }
      userService.createUser(teacher);
      redirectAttributes.addFlashAttribute("success", "Преподаватель успешно добавлен");
    }

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
      User teacher = teacherOpt.get();

      // Проверяем, есть ли занятия у преподавателя
      List<ScheduleEntry> teacherSchedule = scheduleService.findScheduleForTeacher(teacher);
      if (!teacherSchedule.isEmpty()) {
        redirectAttributes.addFlashAttribute("error",
            "Невозможно удалить преподавателя, так как у него есть запланированные занятия. " +
                "Сначала удалите все занятия этого преподавателя.");
        return "redirect:/admin/teachers";
      }

      userService.deleteUser(id);
      redirectAttributes.addFlashAttribute("success", "Преподаватель успешно удален");
    } else {
      redirectAttributes.addFlashAttribute("error", "Преподаватель не найден");
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
      RedirectAttributes redirectAttributes,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/groups/form";
    }

    // Проверка уникальности названия группы
    Optional<StudentGroup> existingGroup = studentGroupService.findGroupByName(group.getName());
    if (existingGroup.isPresent() && (group.getId() == null || !group.getId().equals(existingGroup.get().getId()))) {
      model.addAttribute("nameError", "Группа с таким названием уже существует");
      return "admin/groups/form";
    }

    studentGroupService.saveGroup(group);

    if (group.getId() == null) {
      redirectAttributes.addFlashAttribute("success", "Группа успешно добавлена");
    } else {
      redirectAttributes.addFlashAttribute("success", "Группа успешно обновлена");
    }

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
    // Проверяем существование группы
    Optional<StudentGroup> groupOpt = studentGroupService.findGroupById(id);
    if (!groupOpt.isPresent()) {
      redirectAttributes.addFlashAttribute("error", "Группа не найдена");
      return "redirect:/admin/groups";
    }

    StudentGroup group = groupOpt.get();

    // Проверяем, есть ли занятия у этой группы
    List<ScheduleEntry> groupSchedule = scheduleService.findScheduleForGroup(group);
    if (!groupSchedule.isEmpty()) {
      redirectAttributes.addFlashAttribute("error",
          "Невозможно удалить группу, так как у неё есть запланированные занятия. " +
              "Сначала удалите все занятия этой группы.");
      return "redirect:/admin/groups";
    }

    // Проверяем, есть ли студенты в этой группе
    if (group.getStudents() != null && !group.getStudents().isEmpty()) {
      // Если есть студенты, то отвязываем их от группы перед удалением
      List<User> students = group.getStudents();
      for (User student : students) {
        student.setGroup(null);
        userService.updateUser(student);
      }
    }

    studentGroupService.deleteGroup(id);
    redirectAttributes.addFlashAttribute("success", "Группа успешно удалена");
    return "redirect:/admin/groups";
  }

  // Управление расписанием
  @GetMapping("/schedule")
  public String listSchedule(Model model,
      @RequestParam(required = false) Long teacherId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) Long subjectId,
      @RequestParam(required = false) String lessonType) {
    List<ScheduleEntry> scheduleEntries = scheduleService.findAllScheduleEntries();

    // Применяем фильтры, если они указаны
    if (teacherId != null) {
      Optional<User> teacher = userService.findUserById(teacherId);
      if (teacher.isPresent()) {
        scheduleEntries = scheduleEntries.stream()
            .filter(entry -> entry.getTeacher().getId().equals(teacherId))
            .collect(Collectors.toList());
      }
    }

    if (groupId != null) {
      Optional<StudentGroup> group = studentGroupService.findGroupById(groupId);
      if (group.isPresent()) {
        scheduleEntries = scheduleEntries.stream()
            .filter(entry -> entry.getGroup().getId().equals(groupId))
            .collect(Collectors.toList());
      }
    }

    if (subjectId != null) {
      Optional<Subject> subject = subjectService.findSubjectById(subjectId);
      if (subject.isPresent()) {
        scheduleEntries = scheduleEntries.stream()
            .filter(entry -> entry.getSubject().getId().equals(subjectId))
            .collect(Collectors.toList());
      }
    }

    if (lessonType != null && !lessonType.isEmpty()) {
      try {
        LessonType type = LessonType.valueOf(lessonType);
        scheduleEntries = scheduleEntries.stream()
            .filter(entry -> entry.getLessonType() == type)
            .collect(Collectors.toList());
      } catch (IllegalArgumentException e) {
        // Игнорируем некорректный тип занятия
      }
    }

    // Получаем списки для фильтров
    List<User> teachers = userService.findAllUsers().stream()
        .filter(u -> u.getRole() == UserRole.TEACHER)
        .sorted(Comparator.comparing(User::getFullName))
        .collect(Collectors.toList());

    List<StudentGroup> groups = studentGroupService.findAllGroups();
    groups.sort(Comparator.comparing(StudentGroup::getName));

    List<Subject> subjects = subjectService.findAllSubjects();
    subjects.sort(Comparator.comparing(Subject::getName));

    model.addAttribute("scheduleEntries", scheduleEntries);
    model.addAttribute("teachers", teachers);
    model.addAttribute("groups", groups);
    model.addAttribute("subjects", subjects);
    model.addAttribute("lessonTypes", LessonType.values());

    return "admin/schedule/list";
  }

  @GetMapping("/schedule/new")
  public String newScheduleEntryForm(Model model) {
    ScheduleEntry scheduleEntry = new ScheduleEntry();
    scheduleEntry.setIsRegular(true); // По умолчанию - регулярное занятие

    prepareScheduleFormData(model, scheduleEntry);

    return "admin/schedule/form";
  }

  @PostMapping("/schedule")
  public String saveScheduleEntry(@Valid @ModelAttribute("scheduleEntry") ScheduleEntry scheduleEntry,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (bindingResult.hasErrors()) {
      prepareScheduleFormData(model, scheduleEntry);
      return "admin/schedule/form";
    }

    // Проверяем согласованность данных
    validateScheduleEntry(scheduleEntry);

    // Проверяем конфликты только для новых записей или при изменении
    // времени/даты/ресурсов
    if (scheduleEntry.getId() == null || hasTimeOrResourceChanges(scheduleEntry)) {
      // Проверка конфликтов
      List<ScheduleEntry> teacherConflicts = new ArrayList<>();
      List<ScheduleEntry> groupConflicts = new ArrayList<>();
      List<ScheduleEntry> classroomConflicts = new ArrayList<>();

      LocalDate date = scheduleEntry.getIsRegular()
          ? getNextOccurrenceDate(scheduleEntry.getDayOfWeek(), scheduleEntry.getSemester())
          : scheduleEntry.getSpecificDate();

      if (date != null) {
        // Проверяем доступность преподавателя
        if (!scheduleService.isTeacherAvailable(
            scheduleEntry.getTeacher(),
            date,
            scheduleEntry.getStartTime(),
            scheduleEntry.getEndTime())) {

          teacherConflicts = getTeacherConflicts(scheduleEntry, date);
        }

        // Проверяем доступность группы
        if (!scheduleService.isGroupAvailable(
            scheduleEntry.getGroup(),
            date,
            scheduleEntry.getStartTime(),
            scheduleEntry.getEndTime())) {

          groupConflicts = getGroupConflicts(scheduleEntry, date);
        }

        // Проверяем доступность аудитории
        if (!scheduleService.isClassroomAvailable(
            scheduleEntry.getClassroom(),
            date,
            scheduleEntry.getStartTime(),
            scheduleEntry.getEndTime())) {

          classroomConflicts = getClassroomConflicts(scheduleEntry, date);
        }

        // Если есть конфликты, показываем предупреждения
        if (!teacherConflicts.isEmpty() || !groupConflicts.isEmpty() || !classroomConflicts.isEmpty()) {
          model.addAttribute("teacherConflicts", teacherConflicts);
          model.addAttribute("groupConflicts", groupConflicts);
          model.addAttribute("classroomConflicts", classroomConflicts);

          prepareScheduleFormData(model, scheduleEntry);
          model.addAttribute("error", "Обнаружены конфликты в расписании. Пожалуйста, проверьте и исправьте.");

          return "admin/schedule/form";
        }
      }
    }

    scheduleService.saveScheduleEntry(scheduleEntry);

    if (scheduleEntry.getId() == null) {
      redirectAttributes.addFlashAttribute("success", "Запись в расписании успешно добавлена");
    } else {
      redirectAttributes.addFlashAttribute("success", "Запись в расписании успешно обновлена");
    }

    return "redirect:/admin/schedule";
  }

  @GetMapping("/schedule/edit/{id}")
  public String editScheduleEntryForm(@PathVariable Long id, Model model) {
    Optional<ScheduleEntry> scheduleEntryOpt = scheduleService.findScheduleEntryById(id);
    if (scheduleEntryOpt.isPresent()) {
      ScheduleEntry scheduleEntry = scheduleEntryOpt.get();

      prepareScheduleFormData(model, scheduleEntry);

      return "admin/schedule/form";
    }
    return "redirect:/admin/schedule";
  }

  @GetMapping("/schedule/delete/{id}")
  public String deleteScheduleEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    // Проверяем существование записи
    Optional<ScheduleEntry> scheduleEntryOpt = scheduleService.findScheduleEntryById(id);
    if (!scheduleEntryOpt.isPresent()) {
      redirectAttributes.addFlashAttribute("error", "Запись в расписании не найдена");
      return "redirect:/admin/schedule";
    }

    scheduleService.deleteScheduleEntry(id);
    redirectAttributes.addFlashAttribute("success", "Запись в расписании успешно удалена");
    return "redirect:/admin/schedule";
  }

  // Вспомогательные методы для работы с расписанием

  /**
   * Подготавливает данные для формы добавления/редактирования записи расписания
   */
  private void prepareScheduleFormData(Model model, ScheduleEntry scheduleEntry) {
    // Загружаем списки для выпадающих списков
    List<User> teachers = userService.findAllUsers().stream()
        .filter(u -> u.getRole() == UserRole.TEACHER)
        .sorted(Comparator.comparing(User::getFullName))
        .collect(Collectors.toList());

    List<StudentGroup> groups = studentGroupService.findAllGroups();
    groups.sort(Comparator.comparing(StudentGroup::getName));

    List<Subject> subjects = subjectService.findAllSubjects();
    subjects.sort(Comparator.comparing(Subject::getName));

    List<Classroom> classrooms = classroomService.findAllClassrooms();
    classrooms.sort(Comparator.comparing(Classroom::getNumber));

    List<Semester> semesters = semesterService.findAllSemesters();
    semesters.sort(Comparator.comparing(Semester::getStartDate).reversed());

    model.addAttribute("scheduleEntry", scheduleEntry);
    model.addAttribute("teachers", teachers);
    model.addAttribute("groups", groups);
    model.addAttribute("subjects", subjects);
    model.addAttribute("classrooms", classrooms);
    model.addAttribute("semesters", semesters);
    model.addAttribute("lessonTypes", LessonType.values());
  }

  /**
   * Проверяет согласованность данных в ScheduleEntry
   */
  private void validateScheduleEntry(ScheduleEntry entry) {
    // Если это регулярное занятие, то устанавливаем конкретную дату в null
    if (Boolean.TRUE.equals(entry.getIsRegular())) {
      entry.setSpecificDate(null);
    }
    // Если это разовое занятие, то устанавливаем день недели на основе specificDate
    else {
      if (entry.getSpecificDate() != null) {
        entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
      }
    }

    // Проверяем что время начала меньше времени окончания
    if (entry.getStartTime() != null && entry.getEndTime() != null
        && entry.getStartTime().isAfter(entry.getEndTime())) {
      // Меняем местами
      LocalTime temp = entry.getStartTime();
      entry.setStartTime(entry.getEndTime());
      entry.setEndTime(temp);
    }
  }

  /**
   * Получает следующую дату для заданного дня недели в семестре
   */
  private LocalDate getNextOccurrenceDate(DayOfWeek dayOfWeek, Semester semester) {
    if (dayOfWeek == null || semester == null)
      return null;

    LocalDate currentDate = LocalDate.now();
    LocalDate date = currentDate;

    // Если текущая дата не входит в семестр, берем начало семестра
    if (currentDate.isBefore(semester.getStartDate()) || currentDate.isAfter(semester.getEndDate())) {
      date = semester.getStartDate();
    }

    // Находим ближайший день недели
    while (date.getDayOfWeek() != dayOfWeek) {
      date = date.plusDays(1);
    }

    return date;
  }

  /**
   * Проверяет, были ли изменены время, дата или ресурсы в записи расписания
   */
  private boolean hasTimeOrResourceChanges(ScheduleEntry entry) {
    if (entry.getId() == null)
      return true;

    Optional<ScheduleEntry> existingEntryOpt = scheduleService.findScheduleEntryById(entry.getId());
    if (!existingEntryOpt.isPresent())
      return true;

    ScheduleEntry existingEntry = existingEntryOpt.get();

    // Проверяем изменение ресурсов (преподаватель, группа, аудитория)
    if (!existingEntry.getTeacher().getId().equals(entry.getTeacher().getId()))
      return true;
    if (!existingEntry.getGroup().getId().equals(entry.getGroup().getId()))
      return true;
    if (!existingEntry.getClassroom().getId().equals(entry.getClassroom().getId()))
      return true;

    // Проверяем изменение времени
    if (!Objects.equals(existingEntry.getStartTime(), entry.getStartTime()))
      return true;
    if (!Objects.equals(existingEntry.getEndTime(), entry.getEndTime()))
      return true;

    // Проверяем изменение даты/дня недели
    if (!Objects.equals(existingEntry.getIsRegular(), entry.getIsRegular()))
      return true;

    if (Boolean.TRUE.equals(entry.getIsRegular())) {
      if (!Objects.equals(existingEntry.getDayOfWeek(), entry.getDayOfWeek()))
        return true;
    } else {
      if (!Objects.equals(existingEntry.getSpecificDate(), entry.getSpecificDate()))
        return true;
    }

    return false;
  }

  /**
   * Получает конфликты с расписанием преподавателя
   */
  private List<ScheduleEntry> getTeacherConflicts(ScheduleEntry entry, LocalDate date) {
    // Получаем все занятия преподавателя
    List<ScheduleEntry> teacherSchedule = scheduleService.findScheduleForTeacher(entry.getTeacher());

    // Отфильтровываем текущую запись и находим конфликты
    return teacherSchedule.stream()
        .filter(e -> !e.getId().equals(entry.getId())) // Исключаем текущую запись
        .filter(e -> isTimeConflict(e, entry, date))
        .collect(Collectors.toList());
  }

  /**
   * Получает конфликты с расписанием группы
   */
  private List<ScheduleEntry> getGroupConflicts(ScheduleEntry entry, LocalDate date) {
    // Получаем все занятия группы
    List<ScheduleEntry> groupSchedule = scheduleService.findScheduleForGroup(entry.getGroup());

    // Отфильтровываем текущую запись и находим конфликты
    return groupSchedule.stream()
        .filter(e -> !e.getId().equals(entry.getId())) // Исключаем текущую запись
        .filter(e -> isTimeConflict(e, entry, date))
        .collect(Collectors.toList());
  }

  /**
   * Получает конфликты с расписанием аудитории
   */
  private List<ScheduleEntry> getClassroomConflicts(ScheduleEntry entry, LocalDate date) {
    // Получаем все занятия в аудитории
    List<ScheduleEntry> classroomSchedule = scheduleService.findScheduleForClassroom(entry.getClassroom());

    // Отфильтровываем текущую запись и находим конфликты
    return classroomSchedule.stream()
        .filter(e -> !e.getId().equals(entry.getId())) // Исключаем текущую запись
        .filter(e -> isTimeConflict(e, entry, date))
        .collect(Collectors.toList());
  }

  /**
   * Проверяет, есть ли конфликт по времени между двумя записями расписания
   */
  private boolean isTimeConflict(ScheduleEntry existing, ScheduleEntry newEntry, LocalDate checkDate) {
    // Если записи на разные даты, то конфликта нет
    if (Boolean.FALSE.equals(existing.getIsRegular()) && Boolean.FALSE.equals(newEntry.getIsRegular())) {
      return existing.getSpecificDate().equals(newEntry.getSpecificDate()) &&
          isTimeOverlap(existing.getStartTime(), existing.getEndTime(),
              newEntry.getStartTime(), newEntry.getEndTime());
    }

    // Если одна регулярная, а другая нет
    if (Boolean.TRUE.equals(existing.getIsRegular()) && Boolean.FALSE.equals(newEntry.getIsRegular())) {
      return existing.getDayOfWeek() == newEntry.getSpecificDate().getDayOfWeek() &&
          isTimeOverlap(existing.getStartTime(), existing.getEndTime(),
              newEntry.getStartTime(), newEntry.getEndTime());
    }

    if (Boolean.FALSE.equals(existing.getIsRegular()) && Boolean.TRUE.equals(newEntry.getIsRegular())) {
      return existing.getSpecificDate().getDayOfWeek() == newEntry.getDayOfWeek() &&
          isTimeOverlap(existing.getStartTime(), existing.getEndTime(),
              newEntry.getStartTime(), newEntry.getEndTime());
    }

    // Если обе регулярные
    return existing.getDayOfWeek() == newEntry.getDayOfWeek() &&
        isTimeOverlap(existing.getStartTime(), existing.getEndTime(),
            newEntry.getStartTime(), newEntry.getEndTime());
  }

  /**
   * Проверяет перекрытие временных интервалов
   */
  private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
    return (start1.isBefore(end2) || start1.equals(end2)) &&
        (start2.isBefore(end1) || start2.equals(end1));
  }

  // Управление предметами
  @GetMapping("/subjects")
  public String listSubjects(Model model) {
    List<Subject> subjects = subjectService.findAllSubjects();
    model.addAttribute("subjects", subjects);
    return "admin/subjects/list";
  }

  @GetMapping("/subjects/new")
  public String newSubjectForm(Model model) {
    model.addAttribute("subject", new Subject());
    return "admin/subjects/form";
  }

  @PostMapping("/subjects")
  public String saveSubject(@Valid @ModelAttribute("subject") Subject subject,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/subjects/form";
    }

    // Проверка уникальности названия предмета
    Optional<Subject> existingSubject = subjectService.findSubjectByName(subject.getName());
    if (existingSubject.isPresent()
        && (subject.getId() == null || !subject.getId().equals(existingSubject.get().getId()))) {
      model.addAttribute("nameError", "Предмет с таким названием уже существует");
      return "admin/subjects/form";
    }

    subjectService.saveSubject(subject);

    if (subject.getId() == null) {
      redirectAttributes.addFlashAttribute("success", "Предмет успешно добавлен");
    } else {
      redirectAttributes.addFlashAttribute("success", "Предмет успешно обновлен");
    }

    return "redirect:/admin/subjects";
  }

  @GetMapping("/subjects/edit/{id}")
  public String editSubjectForm(@PathVariable Long id, Model model) {
    Optional<Subject> subjectOpt = subjectService.findSubjectById(id);
    if (subjectOpt.isPresent()) {
      model.addAttribute("subject", subjectOpt.get());
      return "admin/subjects/form";
    }
    return "redirect:/admin/subjects";
  }

  @GetMapping("/subjects/delete/{id}")
  public String deleteSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    // Проверяем существование предмета
    Optional<Subject> subjectOpt = subjectService.findSubjectById(id);
    if (!subjectOpt.isPresent()) {
      redirectAttributes.addFlashAttribute("error", "Предмет не найден");
      return "redirect:/admin/subjects";
    }

    Subject subject = subjectOpt.get();

    // Проверяем, есть ли занятия с этим предметом
    List<ScheduleEntry> subjectSchedule = scheduleService.findScheduleEntriesBySubject(subject);
    if (!subjectSchedule.isEmpty()) {
      redirectAttributes.addFlashAttribute("error",
          "Невозможно удалить предмет, так как с ним связаны запланированные занятия. " +
              "Сначала удалите все занятия по этому предмету.");
      return "redirect:/admin/subjects";
    }

    subjectService.deleteSubject(id);
    redirectAttributes.addFlashAttribute("success", "Предмет успешно удален");
    return "redirect:/admin/subjects";
  }
}