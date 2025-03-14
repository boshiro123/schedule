package com.schedule.config;

import com.schedule.models.UserRole;
import com.schedule.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(UserService userService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public CommandLineRunner initData() {
    return args -> {
      // Проверяем, есть ли уже диспетчер в системе
      if (!userService.existsByUsername("admin")) {
        // Создаем диспетчера (администратора)
        userService.createDispatcher(
            "admin",
            "admin123",
            "Администратор системы",
            "admin@example.com",
            "+7 (999) 123-45-67");
        System.out.println("Создан пользователь с ролью DISPATCHER: admin / admin123");
      }

      // Проверяем, есть ли уже преподаватель в системе
      if (!userService.existsByUsername("teacher")) {
        // Создаем тестового преподавателя
        userService.createTeacher(
            "teacher",
            "teacher123",
            "Иванов Иван Иванович",
            "ivanov@example.com",
            "+7 (999) 765-43-21");
        System.out.println("Создан пользователь с ролью TEACHER: teacher / teacher123");
      }
    };
  }
}