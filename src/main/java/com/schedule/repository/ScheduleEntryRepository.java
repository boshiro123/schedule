package com.schedule.repository;

import com.schedule.models.Classroom;
import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
  List<ScheduleEntry> findByGroup(StudentGroup group);

  List<ScheduleEntry> findByTeacher(User teacher);

  // Методы с сортировкой
  List<ScheduleEntry> findByTeacherOrderBySpecificDateAscStartTimeAsc(User teacher);

  List<ScheduleEntry> findByGroupOrderBySpecificDateAscStartTimeAsc(StudentGroup group);

  List<ScheduleEntry> findByClassroomOrderBySpecificDateAscStartTimeAsc(Classroom classroom);

  // Методы для поиска пересекающихся занятий
  @Query("SELECT s FROM ScheduleEntry s WHERE s.classroom = :classroom AND s.specificDate = :date " +
      "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
  List<ScheduleEntry> findOverlappingEntriesForClassroom(
      @Param("classroom") Classroom classroom,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query("SELECT s FROM ScheduleEntry s WHERE s.group = :group AND s.specificDate = :date " +
      "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
  List<ScheduleEntry> findOverlappingEntriesForGroup(
      @Param("group") StudentGroup group,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query("SELECT s FROM ScheduleEntry s WHERE s.teacher = :teacher AND s.specificDate = :date " +
      "AND ((s.startTime <= :endTime AND s.endTime >= :startTime))")
  List<ScheduleEntry> findOverlappingEntriesForTeacher(
      @Param("teacher") User teacher,
      @Param("date") LocalDate date,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);
}