package com.telegram.bilavorona.handler;

public interface ReportHandler {
    void sendChatHistoryReportToManager(long chatId);

    void sendChatHistoryReportToAllManagers();

    void sendUserStatisticsForWeekToAllManagers();

    void sendUserStatisticsForWeekToManager(long chatId);
}
