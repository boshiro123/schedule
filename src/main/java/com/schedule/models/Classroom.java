package com.schedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classrooms")
public class Classroom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String number;

  @Column
  private String building;

  @Column
  private Integer capacity;

  @Column
  private Boolean hasProjector;

  @Column
  private Boolean hasComputers;

  @Column
  @Enumerated(EnumType.STRING)
  private ClassroomType type;
}