# PowerShell скрипт для развертывания проекта на Windows

# Функция для проверки наличия команды
function Check-Command {
    param([string]$command)
    
    if (Get-Command $command -ErrorAction SilentlyContinue) {
        Write-Host "Команда $command найдена." -ForegroundColor Green
        return $true
    } else {
        Write-Host "Команда $command не найдена." -ForegroundColor Red
        return $false
    }
}

# Функция для установки зависимостей на Windows через Chocolatey
function Install-Windows-Dependencies {
    Write-Host "Установка зависимостей на Windows..." -ForegroundColor Yellow
    
    # Проверка наличия Chocolatey
    if (-not (Check-Command choco)) {
        Write-Host "Устанавливаем Chocolatey..." -ForegroundColor Yellow
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
        
        # Перезагрузка текущей сессии PowerShell
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    }
    
    # Установка Docker Desktop
    if (-not (Check-Command docker)) {
        Write-Host "Устанавливаем Docker Desktop..." -ForegroundColor Yellow
        choco install docker-desktop -y
        Write-Host "Пожалуйста, запустите Docker Desktop и дождитесь его инициализации." -ForegroundColor Yellow
        Write-Host "После запуска Docker Desktop нажмите любую клавишу для продолжения..." -ForegroundColor Yellow
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    # Установка Maven
    if (-not (Check-Command mvn)) {
        Write-Host "Устанавливаем Maven..." -ForegroundColor Yellow
        choco install maven -y
        
        # Перезагрузка текущей сессии PowerShell для обновления PATH
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    }
}

# Функция для проверки статуса Docker
function Check-Docker-Status {
    Write-Host "Проверка статуса Docker..." -ForegroundColor Yellow
    
    try {
        $null = docker info 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Docker запущен и готов к использованию." -ForegroundColor Green
            return $true
        } else {
            Write-Host "Docker не запущен. Пожалуйста, запустите Docker Desktop и повторите попытку." -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "Docker не запущен. Пожалуйста, запустите Docker Desktop и повторите попытку." -ForegroundColor Red
        return $false
    }
}

# Основная функция
function Main {
    Write-Host "=== Скрипт развертывания проекта ===" -ForegroundColor Green
    
    # Проверка, что скрипт запущен от имени администратора
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    
    if (-not $isAdmin) {
        Write-Host "Этот скрипт требует прав администратора для установки зависимостей." -ForegroundColor Red
        Write-Host "Пожалуйста, запустите PowerShell от имени администратора и повторите команду." -ForegroundColor Red
        return
    }
    
    # Установка зависимостей
    Install-Windows-Dependencies
    
    # Проверка наличия требуемых зависимостей
    Write-Host "Проверка необходимых зависимостей..." -ForegroundColor Yellow
    $docker_installed = Check-Command docker
    $mvn_installed = Check-Command mvn
    
    if (-not ($docker_installed -and $mvn_installed)) {
        Write-Host "Необходимые зависимости не установлены. Прерывание скрипта." -ForegroundColor Red
        return
    }
    
    # Проверка статуса Docker
    $docker_running = Check-Docker-Status
    if (-not $docker_running) {
        return
    }
    
    # Сборка проекта
    Write-Host "Сборка проекта с помощью Maven..." -ForegroundColor Yellow
    Invoke-Expression "mvn clean install -DskipTests"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Ошибка сборки проекта." -ForegroundColor Red
        return
    } else {
        Write-Host "Проект успешно собран." -ForegroundColor Green
    }
    
    # Проверка наличия docker-compose или docker compose
    $useDockerCompose = $false
    if (Check-Command "docker-compose") {
        $useDockerCompose = $true
    } else {
        Write-Host "Проверка поддержки docker compose..." -ForegroundColor Yellow
        docker compose version 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Поддерживается команда 'docker compose'." -ForegroundColor Green
            $useDockerCompose = $false
        } else {
            Write-Host "Не найдена поддержка docker-compose или docker compose. Прерывание скрипта." -ForegroundColor Red
            return
        }
    }
    
    # Запуск контейнеров
    Write-Host "Запуск контейнеров с помощью Docker Compose..." -ForegroundColor Yellow
    
    try {
        if ($useDockerCompose) {
            Invoke-Expression "docker-compose down" | Out-Null
            Invoke-Expression "docker-compose up -d"
        } else {
            Invoke-Expression "docker compose down" | Out-Null
            Invoke-Expression "docker compose up -d"
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Ошибка запуска контейнеров." -ForegroundColor Red
            return
        } else {
            Write-Host "Контейнеры успешно запущены." -ForegroundColor Green
            Write-Host "Веб-приложение будет доступно по адресу, указанному в .env файле (SPRING_LOCAL_PORT)." -ForegroundColor Green
            Write-Host "PgAdmin будет доступен по адресу http://localhost:5052" -ForegroundColor Green
            Write-Host "Логин: admin@admin.org" -ForegroundColor Green
            Write-Host "Пароль: admin" -ForegroundColor Green
        }
    } catch {
        Write-Host "Произошла ошибка при запуске контейнеров:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Запуск основной функции
Main 