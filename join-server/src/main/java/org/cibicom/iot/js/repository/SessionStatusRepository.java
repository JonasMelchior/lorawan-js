package org.cibicom.iot.js.repository;

import org.cibicom.iot.js.data.device.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionStatusRepository extends JpaRepository<SessionStatus, Long> {
    Optional<SessionStatus> findByDevEUI(String devEUI);
}
