package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//TODO implement specification and pagination
@Repository
public interface JoinLogRepository extends JpaRepository<JoinLog, Long>, JpaSpecificationExecutor<JoinLog> {
    @Transactional
    List<JoinLog> findByDevice(Device device);
    Page<JoinLog> findByDevice(Device device, Pageable pageable);
    @Transactional
    void deleteAllByDevice(Device device);
    @Transactional
    void deleteByDeviceIn(List<Device> devices);
}
