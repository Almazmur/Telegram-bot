package org.example.telegrambot.bot;

import org.example.telegrambot.entity.User;
import org.example.telegrambot.service.ReportService;
import org.example.telegrambot.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;



public class TelegramBot extends TelegramLongPollingBot {
    private final String botName;
    private final String botToken;

    public static final String STATE_NAME = "WAITING_NAME";
    public static final String STATE_EMAIL = "WAITING_EMAIL";
    public static final String STATE_RATING = "WAITING_RATING";

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private ReportService reportService;

    public TelegramBot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long userId = message.getFrom().getId();
            String text = message.getText();
            String firstName = message.getFrom().getFirstName();
            String lastName = message.getFrom().getLastName();
            String username = message.getFrom().getUserName();

            // Get or create user
            User user = surveyService.getUser(userId);
            if (user == null) {
                user = surveyService.createUser(userId, firstName, lastName, username);
            }

            // Handle commands
            if (text.startsWith("/")) {
                handleCommand(user, message, text);
            } else {
                // Handle form responses
                handleFormResponse(user, message, text);
            }
        }
    }

    private void handleCommand(User user, Message message, String command) {
        Long chatId = message.getChatId();
        switch (command) {
            case "/start":
                surveyService.resetUserState(user);
                sendWelcomeMessage(chatId);
                break;
            case "/form":
                surveyService.resetUserState(user);
                startForm(user, chatId);
                break;
            case "/report":
                surveyService.resetUserState(user);
                generateAndSendReport(chatId);
                break;
            default:
                sendUnknownCommandMessage(chatId);
                break;
        }
    }

    private void handleFormResponse(User user, Message message, String text) {
        Long chatId = message.getChatId();
        String currentState = user.getCurrentState();

        if (currentState == null) {
            sendMessage(chatId, "Пожалуйста, используйте команду \"/form\", чтобы начать опрос");
            return;
        }

        switch (currentState) {
            case STATE_NAME:
                handleNameResponse(user, chatId, text);
                break;
            case STATE_EMAIL:
                handleEmailResponse(user, chatId, text);
                break;
            case STATE_RATING:
                handleRatingResponse(user, chatId, text);
                break;
        }
    }

    private void startForm(User user, Long chatId) {
        user.setCurrentState(STATE_NAME);
        surveyService.saveUser(user);
        sendMessage(chatId, "Введите имя:");
    }

    private void handleNameResponse(User user, Long chatId, String name) {
        user.setFirstName(name);
        user.setCurrentState(STATE_EMAIL);
        surveyService.saveUser(user);
        sendMessage(chatId, "Введите адрес электронной почты:");
    }

    private void handleEmailResponse(User user, Long chatId, String email) {
        if (!surveyService.isValidEmail(email)) {
            sendMessage(chatId, "Некорректный email. Попробуйте снова:");
            return;
        }
        user.setEmail(email);
        user.setCurrentState(STATE_RATING);
        surveyService.saveUser(user);
        sendMessage(chatId, "Пожалуйста, введите оценку от 1 до 10:");
    }

    private void handleRatingResponse(User user, Long chatId, String ratingText) {
        try {
            int rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 10) {
                sendMessage(chatId, "Пожалуйста, введите оценку от 1 до 10:");
                return;
            }

            // Save survey response
            surveyService.saveSurveyResponse(user, user.getFirstName(), user.getEmail(), rating);

            sendMessage(chatId, "Спасибо за участие в опросе!");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите оценку от 1 до 10:");
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Добро пожаловать в наш бот!\n\n" +
                "Доступные команды:\n" +
                "/start - приветственное сообщение\n" +
                "/form - Начать опрос\n" +
                "/report - Получить отчет о результатах опроса";
        sendMessage(chatId, welcomeText);
    }

    private void generateAndSendReport(Long chatId) {
        sendMessage(chatId, "Создание отчета, пожалуйста, подождите...");
        reportService.generateReportAsync(chatId, this);
    }
    public void sendDocument(Long chatId, byte[] documentBytes, String filename, String caption) {
        try {

            InputStream inputStream = new ByteArrayInputStream(documentBytes);
            InputFile document = new InputFile(inputStream, filename);

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(document);
            sendDocument.setCaption(caption);

            execute(sendDocument);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Error generating report. Please try again later.");
            e.printStackTrace();
        }
    }

    private void sendUnknownCommandMessage(Long chatId) {
        sendMessage(chatId, "Неизвестная команда. Пожалуйста, используйте /start, /form, или /report");
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
