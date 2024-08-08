package com.github.jonasmelchior.js.data.device;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "device_group")
public class DeviceGroup {

    @Id
    @GeneratedValue
    private Long id;
    private String groupTitle;

    public DeviceGroup(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public DeviceGroup() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
