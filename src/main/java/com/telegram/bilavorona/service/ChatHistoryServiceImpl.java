package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.ChatHistory;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.repository.ChatHistoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService{
    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    public ChatHistoryServiceImpl(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public List<ChatHistory> getLastMessages(long chatId, int limit) {
        // Retrieve the last 20 messages (or double the limit)
        Pageable pageable = PageRequest.of(0, limit * 2, Sort.by("timestamp").descending());
        List<ChatHistory> chatHistory = chatHistoryRepository.findByUser_ChatIdOrderByTimestampDesc(chatId, pageable);
        Collections.reverse(chatHistory); // Ensure messages are in correct order
        return chatHistory;
    }

    @Override
    public void saveChatMessage(User user, String role, String message) {
        ChatHistory history = new ChatHistory();
        history.setUser(user);
        history.setRole(role);
        history.setMessage(message);
        history.setTimestamp(new Timestamp(System.currentTimeMillis()));
        history.setUsername(user.getUserName());
        chatHistoryRepository.save(history);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void deleteOldChatHistory() {
        Timestamp oneWeekAgo = new Timestamp(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        chatHistoryRepository.deleteByTimestampBefore(oneWeekAgo);
    }

    @Override
    public File generateChatHistoryReport() throws IOException {
        List<ChatHistory> chatHistories = chatHistoryRepository.findAll(); // Get all chat history

        Workbook workbook = new XSSFWorkbook();
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

        // Save file
        File reportFile = new File("ChatHistoryReport" + LocalDate.now() + ".xlsx");
        try (FileOutputStream fileOut = new FileOutputStream(reportFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
        return reportFile;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
