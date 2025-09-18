package org.cibicom.iot.js.service.log;

import org.cibicom.iot.js.data.device.AppSKeyReqLog;
import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.json.JsonPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IAppSKeyReqLogService {
    void save(AppSKeyReqLog appSKeyReqLog);
    void deleteAllByDev(Device device);
    void deleteByDeviceIn(List<Device> devices);
    JsonPage<AppSKeyReqLog> getPageJson(Pageable pageable, Specification<AppSKeyReqLog> appSKeyReqLogSpecification);
    Page<AppSKeyReqLog> getPage(Pageable pageable, Specification<AppSKeyReqLog> appSKeyReqLogSpecification);
}
