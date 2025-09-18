package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.device.DeviceGroup;
import org.cibicom.iot.js.repository.DeviceGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceGroupService implements IDeviceGroupService{

    @Autowired
    private DeviceGroupRepository repository;


    @Override
    public void save(DeviceGroup deviceGroup) {
        repository.save(deviceGroup);
    }

    @Override
    public void delete(DeviceGroup deviceGroup) {
        repository.delete(deviceGroup);
    }

    @Override
    public void deleteAll(List<DeviceGroup> deviceGroups) {
        repository.deleteAll(deviceGroups);
    }

    @Override
    public List<DeviceGroup> findAll() {
        return repository.findAll();
    }
}
