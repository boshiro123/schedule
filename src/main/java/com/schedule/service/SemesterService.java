package com.schedule.service;

import com.schedule.models.Semester;
import com.schedule.repository.SemesterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SemesterService {

  private final SemesterRepository semesterRepository;

  public SemesterService(SemesterRepository semesterRepository) {
    this.semesterRepository = semesterRepository;
  }

  public List<Semester> findAllSemesters() {
    return semesterRepository.findAll();
  }

  public Optional<Semester> findSemesterById(Long id) {
    return semesterRepository.findById(id);
  }

  /**
   * Находит текущий семестр по текущей дате
   * 
   * @return Optional с текущим семестром или empty, если не найден
   */
  public Optional<Semester> findCurrentSemester() {
    LocalDate today = LocalDate.now();
    return semesterRepository.findByDate(today);
  }

  /**
   * Находит текущий семестр или возвращает первый семестр из базы, если текущий
   * не найден
   * 
   * @return Текущий семестр или первый доступный
   */
  public Semester findCurrentSemesterOrDefault() {
    Optional<Semester> currentSemester = findCurrentSemester();

    if (currentSemester.isPresent()) {
      return currentSemester.get();
    }

    // Если текущий семестр не найден, берем первый семестр из базы
    List<Semester> allSemesters = findAllSemesters();
    if (!allSemesters.isEmpty()) {
      return allSemesters.get(0);
    }

    // Если семестров нет, создаем дефолтный (этого не должно происходить в
    // нормальной ситуации)
    Semester defaultSemester = new Semester();
    defaultSemester.setId(1L);
    defaultSemester.setName("Текущий семестр");
    defaultSemester.setStartDate(LocalDate.now().minusMonths(1));
    defaultSemester.setEndDate(LocalDate.now().plusMonths(1));
    defaultSemester.setAcademicYear(LocalDate.now().getYear());
    defaultSemester.setType(com.schedule.models.SemesterType.SPRING);

    return defaultSemester;
  }

  public Semester saveSemester(Semester semester) {
    return semesterRepository.save(semester);
  }

  public void deleteSemester(Long id) {
    semesterRepository.deleteById(id);
  }
}