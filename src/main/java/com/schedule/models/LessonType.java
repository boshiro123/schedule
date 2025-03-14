package com.schedule.models;

public enum LessonType {
  LECTURE("Лекция"),
  SEMINAR("Семинар"),
  LABORATORY("Лабораторная работа"),
  PRACTICE("Практика"),
  CONSULTATION("Консультация"),
  EXAM("Экзамен"),
  TEST("Зачет"),
  ADDITIONAL("Дополнительное занятие");

  private final String displayName;

  LessonType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}