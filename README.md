# Telegram Survey Bot

Telegram бот для проведения опросов и генерации отчетов в формате Word.

## Функционал

- `/start` - Приветственное сообщение
- `/form` - Запуск последовательного опроса (имя → email → оценка 1-10)
- `/report` - Генерация Word-документа с результатами опросов

## Требования

- Docker и Docker Compose
- Telegram Bot Token (получить через @BotFather)

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd telegram-bot
```

### 2. Настройка переменных окружения
Создайте файл .env в корне проекта:

```env
BOT_NAME=YourBotName
BOT_TOKEN=your_telegram_bot_token
DB_USER=postgres
DB_PASSWORD=your_secure_password
```
### 3. Сборка и запуск
```bash
docker-compose up --build
```
Для запуска в фоновом режиме:
```bash
docker-compose up -d --build
```
### 4. Остановка
``` bash
docker-compose down
```
Структура базы данных
Таблица users
* id - ID пользователя Telegram
* first_name - Имя пользователя
* last_name - Фамилия пользователя
* username - Username пользователя
* created_at - Дата создания
* current_state - Текущее состояние формы

Таблица survey_responses
* id - ID ответа
* user_id - Ссылка на пользователя
* name - Имя из опроса
* email - Email из опроса
* rating - Оценка (1-10)
* created_at - Дата создания

### Разработка
Сборка проекта
```bash
mvn clean package
```
### Запуск локально
```bash
mvn spring-boot:run
```
### Технологии: 
Java 11, Spring Boot, Spring Data JPA, PostgreSQL,
Telegram Bot API, Apache POI (для генерации Word документов),Docker,
### Особенности
Асинхронная генерация отчетов, Валидация email адресов, Автоматический сброс состояния формы при прерывании,
Персистентное хранение данных,
Multi-stage Docker build для оптимизации размера образа.

### Основные особенности реализации
* Multi-stage Docker build - оптимизация размера образа
* Асинхронная генерация отчетов - не блокирует работу бота
* Валидация email - проверка корректности ввода
* Сброс состояния формы - при вызове команд /start или /report
* Персистентное хранение - данные сохраняются в PostgreSQL
* Генерация Word документов - с использованием Apache POI

Это полноценное решение, готовое к развертыванию в production среде!