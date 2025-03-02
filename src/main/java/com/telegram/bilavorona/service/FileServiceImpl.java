package com.telegram.bilavorona.service;

import com.telegram.bilavorona.model.FileEntity;
import com.telegram.bilavorona.model.FileGroup;
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

    @Override
    public List<FileEntity> getFilesByGroup(FileGroup group) {
        return fileRepository.findByFileGroup(group);
    }

    @Override
    public boolean deleteFileById(Long id) {
        if (fileRepository.findById(id).isPresent()) {
            fileRepository.deleteById(id);
            return true;  // Successfully deleted
        }
        return false;  // File not found
    }

    @Override
    public boolean deleteFileByName(String fileName) {
        Optional<FileEntity> fileOpt = fileRepository.findByFileName(fileName);
        if (fileOpt.isPresent()) {
            fileRepository.delete(fileOpt.get());
            return true;  // Successfully deleted
        }
        return false;  // File not found
    }

    @Override
    public boolean changeFileGroupById(Long id, FileGroup newGroup) {
        Optional<FileEntity> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            file.setFileGroup(newGroup);
            fileRepository.save(file);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeFileGroupByName(String fileName, FileGroup newGroup) {
        Optional<FileEntity> fileOpt = fileRepository.findByFileName(fileName);
        if (fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            file.setFileGroup(newGroup);
            fileRepository.save(file);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeFileNameById(Long id, String newFileName) {
        Optional<FileEntity> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            file.setFileName(newFileName);
            fileRepository.save(file);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeFileNameByName(String currentFileName, String newFileName) {
        Optional<FileEntity> fileOpt = fileRepository.findByFileName(currentFileName);
        if (fileOpt.isPresent()) {
            FileEntity file = fileOpt.get();
            file.setFileName(newFileName);
            fileRepository.save(file);
            return true;
        }
        return false;
    }


}
