package com.schedule.service;

import com.schedule.models.Classroom;
import com.schedule.repository.ClassroomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassroomService {

  private final ClassroomRepository classroomRepository;

  public ClassroomService(ClassroomRepository classroomRepository) {
    this.classroomRepository = classroomRepository;
  }

  public List<Classroom> findAllClassrooms() {
    return classroomRepository.findAll();
  }

  public Optional<Classroom> findClassroomById(Long id) {
    return classroomRepository.findById(id);
  }

  public Optional<Classroom> findClassroomByNumber(String number) {
    return classroomRepository.findByNumber(number);
  }

  public Classroom saveClassroom(Classroom classroom) {
    return classroomRepository.save(classroom);
  }

  public void deleteClassroom(Long id) {
    classroomRepository.deleteById(id);
  }
}