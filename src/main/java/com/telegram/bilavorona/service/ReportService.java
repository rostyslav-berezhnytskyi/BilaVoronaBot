package com.telegram.bilavorona.service;

import java.io.File;
import java.io.IOException;

public interface ReportService {
    File generateChatHistoryReport() throws IOException;

    File generateAllUsersReport() throws IOException;
}
