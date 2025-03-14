package com.schedule.service;

import com.schedule.models.*;
import com.schedule.repository.ScheduleEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

  private final ScheduleEntryRepository scheduleEntryRepository;

  public ScheduleService(ScheduleEntryRepository scheduleEntryRepository) {
    this.scheduleEntryRepository = scheduleEntryRepository;
  }

  public List<ScheduleEntry> findAllScheduleEntries() {
    return scheduleEntryRepository.findAll();
  }

  public Optional<ScheduleEntry> findScheduleEntryById(Long id) {
    return scheduleEntryRepository.findById(id);
  }

  public List<ScheduleEntry> findScheduleForTeacher(User teacher) {
    return scheduleEntryRepository.findByTeacherOrderBySpecificDateAscStartTimeAsc(teacher);
  }

  public List<ScheduleEntry> findScheduleForGroup(StudentGroup group) {
    return scheduleEntryRepository.findByGroupOrderBySpecificDateAscStartTimeAsc(group);
  }

  public List<ScheduleEntry> findScheduleForClassroom(Classroom classroom) {
    return scheduleEntryRepository.findByClassroomOrderBySpecificDateAscStartTimeAsc(classroom);
  }

  public boolean isClassroomAvailable(Classroom classroom, LocalDate date, LocalTime startTime, LocalTime endTime) {
    // Проверяем, есть ли занятия в этой аудитории в указанное время
    List<ScheduleEntry> overlappingEntries = scheduleEntryRepository.findOverlappingEntriesForClassroom(
        classroom, date, startTime, endTime);

    return overlappingEntries.isEmpty();
  }

  public boolean isGroupAvailable(StudentGroup group, LocalDate date, LocalTime startTime, LocalTime endTime) {
    // Проверяем, есть ли занятия у этой группы в указанное время
    List<ScheduleEntry> overlappingEntries = scheduleEntryRepository.findOverlappingEntriesForGroup(
        group, date, startTime, endTime);

    return overlappingEntries.isEmpty();
  }

  public boolean isTeacherAvailable(User teacher, LocalDate date, LocalTime startTime, LocalTime endTime) {
    // Проверяем, есть ли занятия у этого преподавателя в указанное время
    List<ScheduleEntry> overlappingEntries = scheduleEntryRepository.findOverlappingEntriesForTeacher(
        teacher, date, startTime, endTime);

    return overlappingEntries.isEmpty();
  }

  public ScheduleEntry saveScheduleEntry(ScheduleEntry scheduleEntry) {
    return scheduleEntryRepository.save(scheduleEntry);
  }

  public void deleteScheduleEntry(Long id) {
    scheduleEntryRepository.deleteById(id);
  }
}