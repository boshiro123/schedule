package com.schedule.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utils")
public class UtilController {

  private final PasswordEncoder passwordEncoder;

  public UtilController(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Эндпоинт для шифрования пароля.
   * Пример использования: /api/utils/encode-password?password=mypassword
   * 
   * @param password Пароль для шифрования
   * @return Зашифрованный пароль в формате BCrypt
   */
  @GetMapping(value = { "/encode-password", "/encode-password/" }, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> encodePassword(@RequestParam String password) {
    String encodedPassword = passwordEncoder.encode(password);
    return ResponseEntity.ok(encodedPassword);
  }
}