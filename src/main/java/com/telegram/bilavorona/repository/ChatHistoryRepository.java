package com.telegram.bilavorona.repository;

import com.telegram.bilavorona.model.ChatHistory;
import com.telegram.bilavorona.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUser_ChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);
    void deleteByTimestampBefore(Timestamp timestamp);
}
