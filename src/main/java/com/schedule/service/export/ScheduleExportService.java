package com.schedule.service.export;

import com.schedule.models.ScheduleEntry;
import com.schedule.models.StudentGroup;
import com.schedule.models.User;
import com.schedule.service.ScheduleService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ScheduleExportService {

  private final ScheduleService scheduleService;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  public ScheduleExportService(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  /**
   * Экспорт расписания преподавателя в Excel
   * 
   * @param teacher преподаватель
   * @return массив байтов с Excel-файлом
   * @throws IOException при ошибке записи
   */
  public byte[] exportTeacherScheduleToExcel(User teacher) throws IOException {
    List<ScheduleEntry> entries = scheduleService.findScheduleForTeacher(teacher);

    // Подготовка данных: если день недели не указан, но есть конкретная дата,
    // определяем день недели из даты
    entries.forEach(entry -> {
      if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
        entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
      }
    });

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Расписание преподавателя");

      // Создаем стили для заголовка
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);

      // Создаем заголовок
      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Расписание преподавателя: " + teacher.getFullName());
      titleCell.setCellStyle(headerStyle);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

      // Создаем шапку таблицы
      Row headerRow = sheet.createRow(1);
      String[] columns = { "№", "День недели", "Дата", "Время", "Предмет", "Тип занятия", "Группа", "Аудитория" };

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Заполняем таблицу данными
      int rowNum = 2;
      for (int i = 0; i < entries.size(); i++) {
        ScheduleEntry entry = entries.get(i);
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(i + 1);

        // День недели
        if (entry.getDayOfWeek() != null) {
          String dayOfWeek = entry.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("ru"));
          row.createCell(1).setCellValue(dayOfWeek);
        } else {
          row.createCell(1).setCellValue("-");
        }

        // Дата
        if (entry.getSpecificDate() != null) {
          row.createCell(2).setCellValue(entry.getSpecificDate().format(DATE_FORMATTER));
        } else {
          row.createCell(2).setCellValue("-");
        }

        // Время
        String time = entry.getStartTime().format(TIME_FORMATTER) + " - "
            + entry.getEndTime().format(TIME_FORMATTER);
        row.createCell(3).setCellValue(time);

        // Предмет
        row.createCell(4).setCellValue(entry.getSubject().getName());

        // Тип занятия
        row.createCell(5).setCellValue(entry.getLessonType().getDisplayName());

        // Группа
        row.createCell(6).setCellValue(entry.getGroup().getName());

        // Аудитория
        row.createCell(7).setCellValue(entry.getClassroom().getNumber());
      }

      // Автоматическая настройка ширины столбцов
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  /**
   * Экспорт расписания группы в Excel
   * 
   * @param group группа
   * @return массив байтов с Excel-файлом
   * @throws IOException при ошибке записи
   */
  public byte[] exportGroupScheduleToExcel(StudentGroup group) throws IOException {
    List<ScheduleEntry> entries = scheduleService.findScheduleForGroup(group);

    // Подготовка данных: если день недели не указан, но есть конкретная дата,
    // определяем день недели из даты
    entries.forEach(entry -> {
      if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
        entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
      }
    });

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Расписание группы");

      // Создаем стили для заголовка
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);

      // Создаем заголовок
      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Расписание группы: " + group.getName());
      titleCell.setCellStyle(headerStyle);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

      // Создаем шапку таблицы
      Row headerRow = sheet.createRow(1);
      String[] columns = { "№", "День недели", "Дата", "Время", "Предмет", "Тип занятия", "Преподаватель",
          "Аудитория" };

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Заполняем таблицу данными
      int rowNum = 2;
      for (int i = 0; i < entries.size(); i++) {
        ScheduleEntry entry = entries.get(i);
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(i + 1);

        // День недели
        if (entry.getDayOfWeek() != null) {
          String dayOfWeek = entry.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("ru"));
          row.createCell(1).setCellValue(dayOfWeek);
        } else {
          row.createCell(1).setCellValue("-");
        }

        // Дата
        if (entry.getSpecificDate() != null) {
          row.createCell(2).setCellValue(entry.getSpecificDate().format(DATE_FORMATTER));
        } else {
          row.createCell(2).setCellValue("-");
        }

        // Время
        String time = entry.getStartTime().format(TIME_FORMATTER) + " - "
            + entry.getEndTime().format(TIME_FORMATTER);
        row.createCell(3).setCellValue(time);

        // Предмет
        row.createCell(4).setCellValue(entry.getSubject().getName());

        // Тип занятия
        row.createCell(5).setCellValue(entry.getLessonType().getDisplayName());

        // Преподаватель
        row.createCell(6).setCellValue(entry.getTeacher().getFullName());

        // Аудитория
        row.createCell(7).setCellValue(entry.getClassroom().getNumber());
      }

      // Автоматическая настройка ширины столбцов
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  /**
   * Экспорт всего расписания в Excel (для диспетчера)
   * 
   * @return массив байтов с Excel-файлом
   * @throws IOException при ошибке записи
   */
  public byte[] exportFullScheduleToExcel() throws IOException {
    List<ScheduleEntry> entries = scheduleService.findAllScheduleEntries();

    // Подготовка данных: если день недели не указан, но есть конкретная дата,
    // определяем день недели из даты
    entries.forEach(entry -> {
      if (entry.getDayOfWeek() == null && entry.getSpecificDate() != null) {
        entry.setDayOfWeek(entry.getSpecificDate().getDayOfWeek());
      }
    });

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Полное расписание");

      // Создаем стили для заголовка
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);

      // Создаем заголовок
      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("Полное расписание занятий");
      titleCell.setCellStyle(headerStyle);
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

      // Создаем шапку таблицы
      Row headerRow = sheet.createRow(1);
      String[] columns = { "№", "День недели", "Дата", "Время", "Предмет", "Тип занятия", "Преподаватель", "Группа",
          "Аудитория", "Регулярное" };

      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Заполняем таблицу данными
      int rowNum = 2;
      for (int i = 0; i < entries.size(); i++) {
        ScheduleEntry entry = entries.get(i);
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(i + 1);

        // День недели
        if (entry.getDayOfWeek() != null) {
          String dayOfWeek = entry.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("ru"));
          row.createCell(1).setCellValue(dayOfWeek);
        } else {
          row.createCell(1).setCellValue("-");
        }

        // Дата
        if (entry.getSpecificDate() != null) {
          row.createCell(2).setCellValue(entry.getSpecificDate().format(DATE_FORMATTER));
        } else {
          row.createCell(2).setCellValue("-");
        }

        // Время
        String time = entry.getStartTime().format(TIME_FORMATTER) + " - "
            + entry.getEndTime().format(TIME_FORMATTER);
        row.createCell(3).setCellValue(time);

        // Предмет
        row.createCell(4).setCellValue(entry.getSubject().getName());

        // Тип занятия
        row.createCell(5).setCellValue(entry.getLessonType().getDisplayName());

        // Преподаватель
        row.createCell(6).setCellValue(entry.getTeacher().getFullName());

        // Группа
        row.createCell(7).setCellValue(entry.getGroup().getName());

        // Аудитория
        row.createCell(8).setCellValue(entry.getClassroom().getNumber());

        // Регулярное или разовое
        row.createCell(9).setCellValue(entry.getIsRegular() ? "Да" : "Нет");
      }

      // Автоматическая настройка ширины столбцов
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }
}