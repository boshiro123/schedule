package com.schedule.service;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.repository.ScheduleEntryRepository;
import org.springframework.stereotype.Service;

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

  public List<ScheduleEntry> findScheduleForGroup(StudentGroup group) {
    return scheduleEntryRepository.findByGroup(group);
  }

  public List<ScheduleEntry> findScheduleForTeacher(User teacher) {
    return scheduleEntryRepository.findByTeacher(teacher);
  }

  public ScheduleEntry saveScheduleEntry(ScheduleEntry scheduleEntry) {
    return scheduleEntryRepository.save(scheduleEntry);
  }

  public void deleteScheduleEntry(Long id) {
    scheduleEntryRepository.deleteById(id);
  }
}