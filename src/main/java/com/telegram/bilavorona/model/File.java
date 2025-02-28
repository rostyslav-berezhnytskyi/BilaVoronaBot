package com.telegram.bilavorona.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;       // Назва файлу
    private String fileType;       // Тип файлу (наприклад, "image/png", "application/pdf")
    private Long fileSize;         // Розмір файлу у байтах

    @Lob
    @Column(columnDefinition = "LONGBLOB") // Використовуємо LONGBLOB для зберігання великих файлів
    private byte[] fileData;

    private Long uploadedBy;       // Telegram ID користувача, який завантажив файл
    private Timestamp uploadedAt;  // Дата завантаження

    public File(String fileName, String fileType, Long fileSize, byte[] fileData, Long uploadedBy) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = new Timestamp(System.currentTimeMillis());
    }
}

