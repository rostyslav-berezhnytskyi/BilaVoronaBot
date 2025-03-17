package com.telegram.bilavorona.repository;

import com.telegram.bilavorona.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);

    @Query(value = "SELECT * FROM users_data_table WHERE role IN ('OWNER','ADMIN')", nativeQuery = true)
    List<User> findAllAdmins();

    @Query(value = "SELECT * FROM users_data_table WHERE role = 'BANNED'", nativeQuery = true)
    List<User> findAllBanned();
}
