package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.FileGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileService {
    FileEntity saveFile(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy, LocalDateTime uploadedAt);

    Optional<FileEntity> getFileById(Long id);

    Optional<FileEntity> getFileByName(String fileName);

    List<FileEntity> getFilesByUser(Long userId);

    List<FileEntity> getAllFiles();

    void updateFile(FileEntity file);

    FileEntity getLastUploadedFileByUser(Long userId);

    List<FileEntity> getFilesByGroup(FileGroup group);

    boolean deleteFileById(Long id);

    boolean deleteFileByName(String fileName);

    boolean changeFileGroupById(Long id, FileGroup newGroup);

    boolean changeFileGroupByName(String fileName, FileGroup newGroup);

    boolean changeFileNameById(Long id, String newFileName);

    boolean changeFileNameByName(String currentFileName, String newFileName);
}
