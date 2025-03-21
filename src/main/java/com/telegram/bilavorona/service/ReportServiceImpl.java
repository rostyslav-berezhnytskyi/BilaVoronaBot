package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.ChatHistory;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.repository.ChatHistoryRepository;
import com.telegram.bilavorona.util.MyBotSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    private final UserService userService;
    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    public ReportServiceImpl(UserService userService, ChatHistoryRepository chatHistoryRepository) {
        this.userService = userService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public File generateChatHistoryReport() throws IOException {
        List<ChatHistory> chatHistories = chatHistoryRepository.findAll(); // Get all chat history

        File reportFile = new File("ChatHistoryReport" + LocalDate.now() + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(reportFile)) {
            Sheet sheet = workbook.createSheet("Chat History");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Username", "Role", "Message", "Timestamp"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Fill data rows
            int rowNum = 1;
            for (ChatHistory history : chatHistories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(history.getId());
                row.createCell(1).setCellValue(history.getUsername());
                row.createCell(2).setCellValue(history.getRole());
                row.createCell(3).setCellValue(history.getMessage());
                row.createCell(4).setCellValue(history.getTimestamp().toString());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(fileOut);
        }

        return reportFile;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    @Override
    public File generateAllUsersReport() throws IOException {
        List<User> users = userService.findAll();

        if (users.isEmpty()) {
            throw new IllegalStateException("No users found for the report.");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "First Name", "Last Name", "Username", "Role", "Phone", "Registration Date", "Number of requests to the AI assistant"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getChatId());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getUserName());
            row.createCell(4).setCellValue(user.getRole().toString());
            row.createCell(5).setCellValue(user.getPhoneNumber());
            row.createCell(6).setCellValue(user.getRegisteredAt().toString());
            row.createCell(7).setCellValue(user.getAiMessageCount());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        File reportFile = new File("AllBotUsersBy" + LocalDate.now() + ".xlsx");
        try (FileOutputStream fileOut = new FileOutputStream(reportFile)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }

        return reportFile;
    }
}
