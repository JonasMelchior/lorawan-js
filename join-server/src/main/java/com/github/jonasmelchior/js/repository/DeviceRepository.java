package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    Optional<Device> findDeviceByDevEUI(String devEUI);
    boolean existsByDevEUIIn(List<String> devEUIs);
}
