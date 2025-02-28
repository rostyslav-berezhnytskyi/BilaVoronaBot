package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.File;

import java.util.List;
import java.util.Optional;

public interface FileService {
    File saveFile(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy);

    Optional<File> getFileById(Long id);

    List<File> getFilesByUser(Long userId);
}
