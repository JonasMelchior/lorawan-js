package com.github.jonasmelchior.js.service.log;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import com.github.jonasmelchior.js.repository.JoinLogRepository;
import com.github.jonasmelchior.js.json.JsonPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JoinLogService implements IJoinLogService{
    @Autowired
    private JoinLogRepository repository;

    @Override
    public List<JoinLog> findByDev(Device device) {
        return repository.findByDevice(device);
    }
    public Page<JoinLog> findByDev(Device device, Pageable pageable) {
        return repository.findByDevice(device, pageable);
    }
    @Override
    public void save(JoinLog joinLog) {
        repository.save(joinLog);
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
    public JsonPage<JoinLog> getPageJson(Pageable pageable, Specification<JoinLog> joinLogSpecification) {
        return new JsonPage<>(repository.findAll(joinLogSpecification, pageable), pageable);
    }

    @Override
    public Page<JoinLog> getPage(Pageable pageable, Specification<JoinLog> joinLogSpecification) {
        List<JoinLog> joinLogs = repository.findAll(joinLogSpecification, pageable).getContent();

        return new PageImpl<>(joinLogs, pageable, repository.count(joinLogSpecification));
    }
}
