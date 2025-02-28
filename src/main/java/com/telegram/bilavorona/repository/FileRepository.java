package com.telegram.bilavorona.repository;

import com.telegram.bilavorona.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByFileName(String fileName);
    List<FileEntity> findByUploadedBy(Long uploadedBy);
}
