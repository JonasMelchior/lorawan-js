package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.device.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, Long> {
}
