package com.schedule.repository;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
  List<ScheduleEntry> findByGroup(StudentGroup group);

  List<ScheduleEntry> findByTeacher(User teacher);
}