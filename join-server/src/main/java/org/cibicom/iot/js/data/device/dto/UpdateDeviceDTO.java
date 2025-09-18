package org.cibicom.iot.js.data.device.dto;

import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.lrwan.MACVersion;

import java.util.List;

public class UpdateDeviceDTO {
    private String devEUI;
    private List<KeySpec> keySpecs;
    private Kek kek;
    private Boolean forwardAppSKeyToNS;
    private MACVersion macVersion;

    public UpdateDeviceDTO(String devEUI, List<KeySpec> keySpecs, MACVersion macVersion) {
        this.devEUI = devEUI;
        this.keySpecs = keySpecs;
        this.macVersion = macVersion;
    }

    public Kek getKek() {
        return kek;
    }

    public void setKek(Kek kek) {
        this.kek = kek;
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

    public Boolean getForwardAppSKeyToNS() {
        return forwardAppSKeyToNS;
    }

    public void setForwardAppSKeyToNS(Boolean forwardAppSKeyToNS) {
        this.forwardAppSKeyToNS = forwardAppSKeyToNS;
    }

    @Override
    public String toString() {
        return "UpdateDeviceDTO{" +
                "devEUI='" + devEUI + '\'' +
                ", keySpecs=" + keySpecs +
                ", kek=" + kek +
                ", forwardAppSKeyToNS=" + forwardAppSKeyToNS +
                ", macVersion=" + macVersion +
                '}';
    }
}
