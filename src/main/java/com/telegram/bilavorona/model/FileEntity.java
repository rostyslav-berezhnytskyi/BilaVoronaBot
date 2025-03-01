package com.telegram.bilavorona.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;       // Назва файлу
    private String fileType;       // Тип файлу (наприклад, "image/png", "application/pdf")
    private Long fileSize;         // Розмір файлу у байтах
    @Enumerated(EnumType.STRING)
    private FileGroup fileGroup;      // група файлів, наприклад виконані роботи/документація

    @Lob
    @Column(columnDefinition = "LONGBLOB") // Використовуємо LONGBLOB для зберігання великих файлів
    private byte[] fileData;

    private Long uploadedBy;       // Telegram ID користувача, який завантажив файл
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;  // Дата завантаження

    public FileEntity(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy, LocalDateTime uploadedAt) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }
}

