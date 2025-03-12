package com.schedule.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedule_entries")
public class ScheduleEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "subject_id", nullable = false)
  private Subject subject;

  @ManyToOne
  @JoinColumn(name = "teacher_id", nullable = false)
  private User teacher;

  @ManyToOne
  @JoinColumn(name = "group_id", nullable = false)
  private StudentGroup group;

  @ManyToOne
  @JoinColumn(name = "classroom_id", nullable = false)
  private Classroom classroom;

  @ManyToOne
  @JoinColumn(name = "semester_id", nullable = false)
  private Semester semester;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  @Column
  private LocalDate specificDate;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private LessonType lessonType;

  @Column(nullable = false)
  private Boolean isRegular;

  @Column
  private String comment;
}