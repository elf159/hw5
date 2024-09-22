package com.example.accounts.repositories;

import com.example.accounts.entities.Message;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<Message, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value ="3000")})
    List<Message> findMessages();
}
