package com.schedule.service;

import com.schedule.models.StudentGroup;
import com.schedule.repository.StudentGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentGroupService {

  private final StudentGroupRepository studentGroupRepository;

  public StudentGroupService(StudentGroupRepository studentGroupRepository) {
    this.studentGroupRepository = studentGroupRepository;
  }

  public List<StudentGroup> findAllGroups() {
    return studentGroupRepository.findAll();
  }

  public Optional<StudentGroup> findGroupById(Long id) {
    return studentGroupRepository.findById(id);
  }

  public Optional<StudentGroup> findGroupByName(String name) {
    return studentGroupRepository.findByName(name);
  }

  public StudentGroup saveGroup(StudentGroup group) {
    return studentGroupRepository.save(group);
  }

  public void deleteGroup(Long id) {
    studentGroupRepository.deleteById(id);
  }
}