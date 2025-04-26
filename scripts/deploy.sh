#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Функция для проверки наличия команды
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}Команда $1 не найдена.${NC}"
        return 1
    else
        echo -e "${GREEN}Команда $1 найдена.${NC}"
        return 0
    fi
}

# Функция для установки зависимостей на macOS
install_macos() {
    echo -e "${YELLOW}Установка зависимостей на macOS...${NC}"
    
    # Проверка наличия Homebrew
    if ! check_command brew; then
        echo -e "${YELLOW}Устанавливаем Homebrew...${NC}"
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    
    # Установка Docker
    if ! check_command docker; then
        echo -e "${YELLOW}Устанавливаем Docker...${NC}"
        brew install --cask docker
        echo -e "${YELLOW}Пожалуйста, запустите приложение Docker Desktop и дождитесь его инициализации.${NC}"
        read -p "Нажмите Enter, когда Docker Desktop будет запущен..."
    fi
    
    # Установка Maven
    if ! check_command mvn; then
        echo -e "${YELLOW}Устанавливаем Maven...${NC}"
        brew install maven
    fi
}

# Функция для проверки статуса Docker
check_docker_status() {
    echo -e "${YELLOW}Проверка статуса Docker...${NC}"
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}Docker не запущен. Пожалуйста, запустите Docker и повторите попытку.${NC}"
        return 1
    else
        echo -e "${GREEN}Docker запущен и готов к использованию.${NC}"
        return 0
    fi
}

# Определение OS
detect_os() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "linux"
    else
        echo "unknown"
    fi
}

# Основная функция
main() {
    echo -e "${GREEN}=== Скрипт развертывания проекта ===${NC}"
    
    OS=$(detect_os)
    echo -e "${YELLOW}Обнаружена операционная система: $OS${NC}"
    
    # Установка зависимостей в зависимости от OS
    case $OS in
        "macos")
            install_macos
            ;;
        "linux")
            echo -e "${RED}Автоматическая установка зависимостей для Linux не реализована.${NC}"
            echo -e "${YELLOW}Пожалуйста, установите Docker и Maven вручную.${NC}"
            ;;
        *)
            echo -e "${RED}Неподдерживаемая операционная система.${NC}"
            exit 1
            ;;
    esac
    
    # Проверка наличия требуемых зависимостей
    echo -e "${YELLOW}Проверка необходимых зависимостей...${NC}"
    check_command docker || exit 1
    check_command mvn || exit 1
    
    # Проверка статуса Docker
    check_docker_status || exit 1
    
    # Сборка проекта
    echo -e "${YELLOW}Сборка проекта с помощью Maven...${NC}"
    mvn clean install -DskipTests
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Ошибка сборки проекта.${NC}"
        exit 1
    else
        echo -e "${GREEN}Проект успешно собран.${NC}"
    fi
    
    # Запуск контейнеров
    echo -e "${YELLOW}Запуск контейнеров с помощью Docker Compose...${NC}"
    docker-compose down || true  # Остановка существующих контейнеров (если есть)
    docker-compose up -d
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Ошибка запуска контейнеров.${NC}"
        exit 1
    else
        echo -e "${GREEN}Контейнеры успешно запущены.${NC}"
        echo -e "${GREEN}Веб-приложение будет доступно по адресу, указанному в .env файле (SPRING_LOCAL_PORT).${NC}"
        echo -e "${GREEN}PgAdmin будет доступен по адресу http://localhost:5052${NC}"
        echo -e "${GREEN}Логин: admin@admin.org${NC}"
        echo -e "${GREEN}Пароль: admin${NC}"
    fi
}

# Запуск основной функции
main 