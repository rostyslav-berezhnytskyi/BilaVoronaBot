package com.telegram.bilavorona.handler;

import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.service.ReportService;
import com.telegram.bilavorona.service.UserService;
import com.telegram.bilavorona.util.MyBotSender;
import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ReportHandlerImpl implements ReportHandler {
    private final ReportService reportService;
    private final RoleValidator roleValidator;
    private final MyBotSender botSender;
    private final UserService userService;

    @Autowired
    public ReportHandlerImpl(ReportService reportService, RoleValidator roleValidator, MyBotSender botSender, UserService userService) {
        this.reportService = reportService;
        this.roleValidator = roleValidator;
        this.botSender = botSender;
        this.userService = userService;
    }

    @Override
    public void sendChatHistoryReportToManager(long chatId) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        File reportFile = generateChatHistoryReport();
        if (reportFile == null) {
            botSender.sendMessage(chatId, "Не зміг відправити файл репортом");
            return;
        }
        botSender.sendDocumentFile(chatId, reportFile, "📊 Історія чату всіх користувачів з АІ асистентом за минулий тиждень");
    }

    @Override
    public void sendChatHistoryReportToAllManagers() {
        File reportFile = generateChatHistoryReport();
        if (reportFile == null) return;

        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                botSender.sendDocumentFile(manager.getChatId(), reportFile, "📊 Історія чату всіх користувачів з АІ асистентом за минулий тиждень");
            } catch (Exception e) {
                log.error("Cant send message to manager with id {}", manager.getChatId());
            }

        }
    }

    private File generateChatHistoryReport() {
        try {
            File reportFile = reportService.generateChatHistoryReport();
            if (reportFile != null && reportFile.exists()) {
                return reportFile;
            } else {
                log.warn("Chat history report file was not generated.");
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to generate chat history report", e);
            return null;
        }
    }

    @Override
    public void sendUserStatisticsForWeekToManager(long chatId) {
        if (!roleValidator.checkRoleOwnerOrAdmin(chatId)) return;

        File reportFile = generateUserStatisticsReport();
        if (reportFile == null) {
            botSender.sendMessage(chatId, "Не зміг відправити файл репортом");
            return;
        }
        botSender.sendDocumentFile(chatId, reportFile, "📊 Статистика використання бота користувачами за минулий тиждень");
    }

    @Override
    public void sendUserStatisticsForWeekToAllManagers() {
        File reportFile = generateUserStatisticsReport();
        if (reportFile == null) return;

        List<User> admins = userService.findAllAdmins();
        for (User manager : admins) {
            try {
                botSender.sendDocumentFile(manager.getChatId(), reportFile, "📊 Статистика використання бота користувачами за минулий тиждень");
            } catch (Exception e) {
                log.error("Cant send message to manager with id {}", manager.getChatId());
            }

        }
    }

    private File generateUserStatisticsReport() {
        try {
            File reportFile = reportService.generateUserStatisticsReport();
            if (reportFile != null && reportFile.exists()) {
                return reportFile;
            } else {
                log.warn("User statistics report file was not generated.");
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to generate user statistics report", e);
            return null;
        }
    }

}
