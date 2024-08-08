package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.json.JsonPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface IDeviceService {
    List<Device> findAll();
    Optional<Device> findByDevEUI(String devEUI);
    Boolean isDuplicate(List<String> devEUIs);
    void save(Device device, Boolean update);
    void saveAll(List<Device> devices, Boolean update);
    void delete(Device device);
    void deleteAll(List<Device> devices);
    JsonPage<Device> getPageJSon(Pageable pageable, Specification<Device> deviceSpecification);
    Page<Device> getPage(Pageable pageable, Specification<Device> deviceSpecification);
}
