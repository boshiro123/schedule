package com.schedule.service;

import com.schedule.models.Subject;
import com.schedule.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

  private final SubjectRepository subjectRepository;

  public SubjectService(SubjectRepository subjectRepository) {
    this.subjectRepository = subjectRepository;
  }

  public List<Subject> findAllSubjects() {
    return subjectRepository.findAll();
  }

  public Optional<Subject> findSubjectById(Long id) {
    return subjectRepository.findById(id);
  }

  public Optional<Subject> findSubjectByName(String name) {
    return subjectRepository.findByName(name);
  }

  public Subject saveSubject(Subject subject) {
    return subjectRepository.save(subject);
  }

  public void deleteSubject(Long id) {
    subjectRepository.deleteById(id);
  }
}