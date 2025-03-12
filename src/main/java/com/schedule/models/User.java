package com.schedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column
  private String email;

  @Column
  private String phoneNumber;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private StudentGroup group;
}