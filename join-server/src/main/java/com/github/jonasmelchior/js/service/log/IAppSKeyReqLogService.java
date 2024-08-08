package com.github.jonasmelchior.js.service.log;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.json.JsonPage;
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
