package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.File;
import com.telegram.bilavorona.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService{
    private final FileRepository fileRepository;

    @Override
    public File saveFile(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy) {
        File file = new File(fileName, fileType, fileSize, fileData, uploadedBy);
        return fileRepository.save(file);
    }

    @Override
    public Optional<File> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    @Override
    public List<File> getFilesByUser(Long userId) {
        return fileRepository.findByUploadedBy(userId);
    }
}
