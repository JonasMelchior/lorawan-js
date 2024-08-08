package com.github.jonasmelchior.js.service.log;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import com.github.jonasmelchior.js.repository.AppSKeyReqLogRepository;
import com.github.jonasmelchior.js.json.JsonPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppSKeyReqLogService implements IAppSKeyReqLogService{

    @Autowired
    private AppSKeyReqLogRepository repository;

    @Override
    public void save(AppSKeyReqLog appSKeyReqLog) {
        repository.save(appSKeyReqLog);
    }

    @Override
    public void deleteAllByDev(Device device) {
        repository.deleteAllByDevice(device);
    }

    @Override
    public void deleteByDeviceIn(List<Device> devices) {
        repository.deleteByDeviceIn(devices);
    }

    @Override
    public JsonPage<AppSKeyReqLog> getPageJson(Pageable pageable, Specification<AppSKeyReqLog> appSKeyReqLogSpecification) {
        return new JsonPage<>(repository.findAll(appSKeyReqLogSpecification, pageable), pageable);

    }

    @Override
    public Page<AppSKeyReqLog> getPage(Pageable pageable, Specification<AppSKeyReqLog> appSKeyReqLogSpecification) {
        List<AppSKeyReqLog> appSKeyReqLogs = repository.findAll(appSKeyReqLogSpecification, pageable).getContent();

        return new PageImpl<>(appSKeyReqLogs, pageable, repository.count(appSKeyReqLogSpecification));
    }
}
