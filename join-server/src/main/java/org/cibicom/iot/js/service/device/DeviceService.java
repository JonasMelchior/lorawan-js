package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.repository.DeviceRepository;
import org.cibicom.iot.js.json.JsonPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService implements IDeviceService{

    @Autowired
    private DeviceRepository repository;

    @Override
    public List<Device> findAll() {
        return repository.findAll();
    }

    public Optional<Device> findByDevEUI(String devEUI) {
        return repository.findDeviceByDevEUI(devEUI.toUpperCase());
    }

    @Override
    public Boolean isDuplicate(List<String> devEUIs) {
        return repository.existsByDevEUIIn(devEUIs);
    }

    @Override
    public void save(Device device, Boolean update) {
        device.setDevEUI(device.getDevEUI().toUpperCase());
        if (update != null) {
            if (!update) {
                device.setCreatedAt(LocalDateTime.now());
            }
            else {
                device.setUpdatedAt(LocalDateTime.now());
            }
        }

        repository.save(device);
    }

    @Override
    public void saveAll(List<Device> devices, Boolean update) {
        for (Device device : devices) {
            device.setDevEUI(device.getDevEUI().toUpperCase());
            if (!update) {
                device.setCreatedAt(LocalDateTime.now());
            }
            else {
                device.setUpdatedAt(LocalDateTime.now());
            }
        }
        repository.saveAll(devices);
    }

    @Override
    public void delete(Device device) {
        repository.delete(device);
    }

    @Override
    public void deleteAll(List<Device> devices) {
        repository.deleteAll(devices);
    }

    @Override
    public JsonPage<Device> getPageJSon(Pageable pageable, Specification<Device> deviceSpecification) {
        return new JsonPage<>(repository.findAll(deviceSpecification, pageable), pageable);
    }

    @Override
    public Page<Device> getPage(Pageable pageable, Specification<Device> deviceSpecification) {
        List<Device> devices = repository.findAll(deviceSpecification, pageable).getContent();

        return new PageImpl<>(devices, pageable, repository.count(deviceSpecification));
    }

}
