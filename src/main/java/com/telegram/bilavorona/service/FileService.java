package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.FileEntity;

import java.util.List;
import java.util.Optional;

public interface FileService {
    FileEntity saveFile(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy);

    Optional<FileEntity> getFileById(Long id);

    Optional<FileEntity> getFileByName(String fileName);

    List<FileEntity> getFilesByUser(Long userId);

    List<FileEntity> getAllFiles();
}
