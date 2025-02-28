package com.telegram.bilavorona.repository;

import com.telegram.bilavorona.model.File;
import org.apache.http.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByFileName(String fileName);
    List<File> findByUploadedBy(Long uploadedBy);
}
