package com.telegram.bilavorona.handler;

public interface ReportHandler {
    void sendChatHistoryReportToManager(long chatId);

    void sendChatHistoryReportToAllManagers();
}
