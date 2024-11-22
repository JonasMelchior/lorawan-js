package com.github.jonasmelchior.js.data.device.dto;

import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.dto.Credential;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;

import java.util.List;

public class CreateDeviceDTO {
    private String devEUI;
    private List<KeySpec> keySpecs;
    private Kek kek;
    private MACVersion macVersion;
    private Credential credential;

    public CreateDeviceDTO(String devEUI, List<KeySpec> keySpecs, MACVersion macVersion) {
        this.devEUI = devEUI;
        this.keySpecs = keySpecs;
        this.macVersion = macVersion;
    }

    public CreateDeviceDTO() {
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public List<KeySpec> getKeySpecs() {
        return keySpecs;
    }

    public void setKeySpecs(List<KeySpec> keySpecs) {
        this.keySpecs = keySpecs;
    }

    public MACVersion getMacVersion() {
        return macVersion;
    }

    public void setMacVersion(MACVersion macVersion) {
        this.macVersion = macVersion;
    }

    public Kek getKek() {
        return kek;
    }

    public void setKek(Kek kek) {
        this.kek = kek;
    }

    @Override
    public String toString() {
        return "CreateDeviceDTO{" +
                "devEUI='" + devEUI + '\'' +
                ", keySpecs=" + keySpecs +
                ", kek=" + kek +
                ", macVersion=" + macVersion +
                ", credential=" + credential +
                '}';
    }
}
