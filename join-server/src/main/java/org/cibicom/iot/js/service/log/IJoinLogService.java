package org.cibicom.iot.js.service.log;

import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.data.device.JoinLog;
import org.cibicom.iot.js.json.JsonPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IJoinLogService {
    List<JoinLog> findByDev(Device device);
    void save(JoinLog joinLog);
    void deleteAllByDev(Device device);
    void deleteByDeviceIn(List<Device> devices);
    JsonPage<JoinLog> getPageJson(Pageable pageable, Specification<JoinLog> joinLogSpecification);
    Page<JoinLog> getPage(Pageable pageable, Specification<JoinLog> joinLogSpecification);
}
