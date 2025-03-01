package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService{
    private final FileRepository fileRepository;

    @Override
    public FileEntity saveFile(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy, LocalDateTime uploadedAt) {
        FileEntity file = new FileEntity(fileName, fileType, fileSize, fileData, uploadedBy, uploadedAt);
        return fileRepository.save(file);
    }

    @Override
    public Optional<FileEntity> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    @Override
    public Optional<FileEntity> getFileByName(String fileName) {
        return fileRepository.findByFileName(fileName);
    }

    @Override
    public List<FileEntity> getFilesByUser(Long userId) {
        return fileRepository.findByUploadedBy(userId);
    }

    @Override
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    @Override
    public void updateFile(FileEntity file) {
        fileRepository.save(file);
    }

    @Override
    public FileEntity getLastUploadedFileByUser(Long userId) {
        return fileRepository.findTopByUploadedByOrderByUploadedAtDesc(userId);
    }
}
