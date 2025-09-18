package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.device.DeviceGroup;

import java.util.List;

public interface IDeviceGroupService {
    void save(DeviceGroup deviceGroup);
    void delete(DeviceGroup deviceGroup);
    void deleteAll(List<DeviceGroup> deviceGroups);
    List<DeviceGroup> findAll();
}
