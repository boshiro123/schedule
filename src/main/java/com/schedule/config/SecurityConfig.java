package com.schedule.config;

import com.schedule.models.UserRole;
import com.schedule.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;

  public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/login", "/api/utils/**")) // Отключаем CSRF для страницы входа и API утилит
        .authorizeHttpRequests(authorize -> authorize
            // Публичные страницы
            .requestMatchers("/", "/schedule/public/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
            // API утилиты
            .requestMatchers("/api/utils/**").permitAll()
            // Страницы для диспетчера (админа)
            .requestMatchers("/admin/**").hasAuthority(UserRole.DISPATCHER.name())
            // Страницы для преподавателя
            .requestMatchers("/teacher/**").hasAuthority(UserRole.TEACHER.name())
            // Остальные страницы требуют аутентификации
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard")
            .permitAll())
        .logout(logout -> logout
            .logoutSuccessUrl("/")
            .permitAll());

    return http.build();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}