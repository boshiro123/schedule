package com.schedule.repository;

import com.schedule.models.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

  @Query("SELECT s FROM Semester s WHERE :date BETWEEN s.startDate AND s.endDate")
  Optional<Semester> findByDate(LocalDate date);
}