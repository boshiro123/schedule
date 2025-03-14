package com.schedule.service;

import com.schedule.models.User;
import com.schedule.models.UserRole;
import com.schedule.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  public Optional<User> findUserById(Long id) {
    return userRepository.findById(id);
  }

  public Optional<User> findUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public User createUser(User user) {
    // Шифруем пароль перед сохранением
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
  }

  public User updateUser(User user) {
    // Если пароль не изменился (не содержит хеш), то шифруем его
    if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
    return userRepository.save(user);
  }

  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public User createTeacher(String username, String password, String fullName, String email, String phoneNumber) {
    User teacher = new User();
    teacher.setUsername(username);
    teacher.setPassword(passwordEncoder.encode(password));
    teacher.setFullName(fullName);
    teacher.setEmail(email);
    teacher.setPhoneNumber(phoneNumber);
    teacher.setRole(UserRole.TEACHER);
    return userRepository.save(teacher);
  }

  public User createDispatcher(String username, String password, String fullName, String email, String phoneNumber) {
    User dispatcher = new User();
    dispatcher.setUsername(username);
    dispatcher.setPassword(passwordEncoder.encode(password));
    dispatcher.setFullName(fullName);
    dispatcher.setEmail(email);
    dispatcher.setPhoneNumber(phoneNumber);
    dispatcher.setRole(UserRole.DISPATCHER);
    return userRepository.save(dispatcher);
  }

  public boolean changePassword(Long userId, String oldPassword, String newPassword) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (passwordEncoder.matches(oldPassword, user.getPassword())) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
      }
    }
    return false;
  }
}