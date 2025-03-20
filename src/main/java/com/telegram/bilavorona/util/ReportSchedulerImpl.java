package com.telegram.bilavorona.util;

import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.ChatHistoryService;
import com.telegram.bilavorona.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ReportSchedulerImpl implements ReportScheduler {
    private final ChatHistoryService chatHistoryService;
    private final MyBotSender botSender;
    private final UserService userService;

    @Autowired
    public ReportSchedulerImpl(ChatHistoryService chatHistoryReportService, MyBotSender botSender, UserService userService) {
        this.chatHistoryService = chatHistoryReportService;
        this.botSender = botSender;
        this.userService = userService;
    }

    //    @Scheduled(cron = "0 0 8 * * MON") // Every Monday at 08:00 AM
    @Scheduled(cron = "0 * * * * ?")
    public void sendChatHistoryReport() {
        try {
            File reportFile = chatHistoryService.generateChatHistoryReport();
            sendFileToAllManagers(reportFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFileToAllManagers(File file) {
        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                botSender.sendDocumentFile(manager.getChatId(), file, "📊 Chat History Report for the Week");
            } catch (Exception e) {
                log.error("Failed to send report to user: {}", manager.getChatId(), e);
                botSender.sendMessage(manager.getChatId(), "Не зміг відправити файл з історією запитів до АІ чат бота");
            }
        }
    }
}
