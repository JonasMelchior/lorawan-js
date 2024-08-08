package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.keys.DevKeyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevKeyIdRepository extends JpaRepository<DevKeyId, Long> {
    Optional<DevKeyId> findByDevEUI(String devEUI);
    List<DevKeyId> findAllByDevEUIIn(List<String> devEUIIs);
}
