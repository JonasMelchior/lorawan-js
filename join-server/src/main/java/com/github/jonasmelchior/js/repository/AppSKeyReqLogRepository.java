package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AppSKeyReqLogRepository extends JpaRepository<AppSKeyReqLog, Long>, JpaSpecificationExecutor<AppSKeyReqLog> {
    @Transactional
    void deleteAllByDevice(Device device);
    @Transactional
    void deleteByDeviceIn(List<Device> devices);
}
