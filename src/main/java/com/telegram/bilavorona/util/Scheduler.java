package com.telegram.bilavorona.util;

import com.telegram.bilavorona.handler.ReportHandler;
import com.telegram.bilavorona.service.ChatHistoryService;
import com.telegram.bilavorona.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Scheduler {
    private final ChatHistoryService chatHistoryService;
    private final ReportHandler reportHandler;

    @Autowired
    public Scheduler(ChatHistoryService chatHistoryService, ReportHandler reportHandler) {
        this.chatHistoryService = chatHistoryService;
        this.reportHandler = reportHandler;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 01:00
    public void deleteOldChatHistory() {
        chatHistoryService.deleteOldChatHistory();
        log.info("Delete old chat history");
    }

    @Scheduled(cron = "0 0 10 * * MON") // Every Monday at 10:00 AM
    public void sendChatHistoryReport() {
        reportHandler.sendChatHistoryReportToAllManagers();
        log.info("Send chat history report");
    }
}
