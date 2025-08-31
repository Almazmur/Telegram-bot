package org.example.telegrambot.service;

import org.apache.poi.xwpf.usermodel.*;
import org.example.telegrambot.bot.TelegramBot;
import org.example.telegrambot.entity.SurveyResponse;
import org.example.telegrambot.repository.SurveyResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;


    public byte[] generateReport() throws IOException {
        List<SurveyResponse> responses = surveyResponseRepository.findAllByOrderByCreatedAtDesc();

        try (XWPFDocument document = new XWPFDocument()) {
            // Create title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Отчет об результатах опроса");
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // Add empty line
            document.createParagraph();

            // Create table
            XWPFTable table = document.createTable();

            // Create header row
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("Name");
            headerRow.addNewTableCell().setText("Email");
            headerRow.addNewTableCell().setText("Rating");

            // Add data rows
            for (SurveyResponse response : responses) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(response.getName() != null ? response.getName() : "");
                row.getCell(1).setText(response.getEmail() != null ? response.getEmail() : "");
                row.getCell(2).setText(response.getRating() != null ? response.getRating().toString() : "");
            }

            // Write to byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    @Async
    public void generateReportAsync(Long chatId, TelegramBot bot) {
        try {
            byte[] reportBytes = generateReport();
            bot.sendDocument(chatId, reportBytes, "survey_report.docx", "Вот ваш отчет по результатам опроса");
        } catch (Exception e) {
            bot.sendMessage(chatId, "Ошибка при создании отчета. Пожалуйста, попробуйте позже.");
            e.printStackTrace();
        }
    }
}
