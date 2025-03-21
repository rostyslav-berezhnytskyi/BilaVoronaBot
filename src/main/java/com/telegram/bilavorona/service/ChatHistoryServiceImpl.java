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
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    public ChatHistoryServiceImpl(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    public List<ChatHistory> getLastMessages(long chatId, int limit) {
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

    @Override
    public void deleteOldChatHistory() {
        Timestamp oneWeekAgo = new Timestamp(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000));
        chatHistoryRepository.deleteByTimestampBefore(oneWeekAgo);
    }
}
