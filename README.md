# Автоматизация составления расписания в вузе

## Описание проекта

Система автоматизации составления расписания в вузе предназначена для упрощения процесса создания, редактирования и просмотра расписания занятий. Система позволяет диспетчерам составлять расписание, учитывая доступность аудиторий, преподавателей и групп студентов, а также предоставляет возможность просмотра расписания для студентов и преподавателей.

## План разработки проекта

### 1. Подготовка и настройка проекта

- [x] Создание базовой структуры проекта на Spring Boot
- [x] Настройка подключения к базе данных PostgreSQL
- [x] Определение моделей данных
- [x] Настройка миграций базы данных (Liquibase)
- [x] Настройка Spring Security для авторизации и аутентификации

### 2. Разработка базовых функциональностей

- [x] Реализация репозиториев для работы с данными
- [x] Разработка сервисов для бизнес-логики
- [x] Создание REST API для основных операций
- [x] Реализация авторизации и аутентификации пользователей
- [x] Разработка функционала управления пользователями (регистрация, изменение пароля)

### 3. Разработка функционала для диспетчера

- [x] Реализация управления данными о группах студентов
- [x] Реализация управления данными о преподавателях
- [x] Реализация управления данными о дисциплинах
- [x] Разработка функционала составления расписания
- [x] Реализация возможности внесения изменений в существующие расписания
- [x] Разработка функционала создания дополнительных занятий

### 4. Разработка функционала для студентов и преподавателей

- [x] Реализация просмотра расписания для студентов
- [x] Реализация просмотра расписания для преподавателей
- [x] Разработка фильтрации и поиска в расписании

### 5. Разработка функционала экспорта данных

- [x] Реализация выгрузки расписания в Excel-таблицу
- [x] Разработка форматирования и стилизации выгружаемых данных

### 6. Разработка пользовательского интерфейса

- [x] Создание макетов пользовательского интерфейса
- [x] Разработка фронтенд-части для диспетчера
- [x] Разработка фронтенд-части для студентов и преподавателей
- [x] Реализация адаптивного дизайна

### 7. Тестирование

- [ ] Написание модульных тестов
- [ ] Написание интеграционных тестов
- [ ] Проведение функционального тестирования
- [ ] Тестирование пользовательского интерфейса

### 8. Оптимизация и улучшение

- [ ] Оптимизация производительности
- [ ] Улучшение пользовательского опыта
- [ ] Рефакторинг кода

### 9. Документация и развертывание

- [ ] Написание документации по API
- [ ] Создание руководства пользователя
- [ ] Настройка CI/CD
- [ ] Подготовка к развертыванию в продакшн

## Технологии

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- Hibernate
- Maven
- Docker
- Thymeleaf
- Bootstrap
- Liquibase
- JUnit
- Mockito

## Авторизация и доступ

- **Студенты**: могут просматривать расписание без авторизации, просто выбрав свою группу на главной странице
- **Преподаватели**: аккаунты создаются диспетчером, могут просматривать своё расписание после авторизации
- **Диспетчер**: выступает в роли администратора системы, имеет доступ к панели администратора для управления всеми данными

## Учетные данные для тестирования

- **Диспетчер**: admin / admin123
- **Преподаватель**: teacher / teacher123
- **Преподаватель 2**: teacher2 / teacher123
