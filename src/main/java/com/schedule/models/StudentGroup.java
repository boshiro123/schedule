package com.schedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student_groups")
public class StudentGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private Integer year;

  @Column(nullable = false)
  private String faculty;

  @Column(nullable = false)
  private String specialization;

  @Column
  private Integer numberOfStudents;

  @OneToMany(mappedBy = "group")
  private List<User> students;
}