package com.telegram.bilavorona.util;

import com.telegram.bilavorona.handler.ReportHandler;
import com.telegram.bilavorona.handler.UserStatisticsHandler;
import com.telegram.bilavorona.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Scheduler {
    private final ChatHistoryService chatHistoryService;
    private final ReportHandler reportHandler;
    private final UserStatisticsHandler userStatisticsHandler;

    @Autowired
    public Scheduler(ChatHistoryService chatHistoryService, ReportHandler reportHandler, UserStatisticsHandler userStatisticsHandler) {
        this.chatHistoryService = chatHistoryService;
        this.reportHandler = reportHandler;
        this.userStatisticsHandler = userStatisticsHandler;
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

    @Scheduled(cron = "0 0 23 * * ?") // Every day at 23:00
    public void sendUserStatisticsDailyReport() {
        userStatisticsHandler.sendUserStatisticsForDayToAllManagers();
    }

    @Scheduled(cron = "0 0 23 ? * SUN") // Every Sunday at 23:00
    public void sendUserStatisticsWeeklyReport() {
        userStatisticsHandler.sendUserStatisticsForWeekToAllManagers();
    }

    @Scheduled(cron = "0 0 23 L * ?") // Last day of the month at 23:00
    public void sendUserStatisticsMonthlyReport() {
        userStatisticsHandler.sendUserStatisticsForMonthToAllManagers();
    }

    @Scheduled(cron = "0 0 23 ? * SUN") // Every Monday at 10:00 AM
    public void sendUserStatisticsReport() {
        reportHandler.sendUserStatisticsForWeekToAllManagers();
        log.info("Send chat history report");
    }
}
