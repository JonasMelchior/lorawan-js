package com.github.jonasmelchior.js.data.device.dto;

import com.github.jonasmelchior.js.data.device.Device;

public class UpdateCreateDeviceResponseDTO {
    private String devEUI;
    private Boolean rootKeysExposed;

    public UpdateCreateDeviceResponseDTO(String devEUI, Boolean rootKeysExposed) {
        this.devEUI = devEUI;
        this.rootKeysExposed = rootKeysExposed;
    }

    public UpdateCreateDeviceResponseDTO(Device device) {
        this.devEUI = device.getDevEUI();
        this.rootKeysExposed = device.getRootKeysExposed();
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public Boolean getRootKeysExposed() {
        return rootKeysExposed;
    }

    public void setRootKeysExposed(Boolean rootKeysExposed) {
        this.rootKeysExposed = rootKeysExposed;
    }
}
