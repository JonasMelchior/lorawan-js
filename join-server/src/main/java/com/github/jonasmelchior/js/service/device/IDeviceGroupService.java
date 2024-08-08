package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.device.DeviceGroup;

import java.util.List;

public interface IDeviceGroupService {
    void save(DeviceGroup deviceGroup);
    void delete(DeviceGroup deviceGroup);
    void deleteAll(List<DeviceGroup> deviceGroups);
    List<DeviceGroup> findAll();
}
