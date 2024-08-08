package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.device.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionStatusRepository extends JpaRepository<SessionStatus, Long> {
    Optional<SessionStatus> findByDevEUI(String devEUI);
}
