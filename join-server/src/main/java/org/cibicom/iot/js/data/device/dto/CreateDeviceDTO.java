package org.cibicom.iot.js.data.device.dto;

import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.dto.Credential;
import org.cibicom.iot.js.data.lrwan.MACVersion;

import java.util.List;

public class CreateDeviceDTO {
    private String devEUI;
    private List<KeySpec> keySpecs;
    private Kek kek;
    private Boolean forwardAppSKeyToNS;
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

    public Boolean getForwardAppSKeyToNS() {
        return forwardAppSKeyToNS;
    }

    public void setForwardAppSKeyToNS(Boolean forwardAppSKeyToNS) {
        this.forwardAppSKeyToNS = forwardAppSKeyToNS;
    }

    @Override
    public String toString() {
        return "CreateDeviceDTO{" +
                "devEUI='" + devEUI + '\'' +
                ", keySpecs=" + keySpecs +
                ", kek=" + kek +
                ", forwardAppSKeyToNS=" + forwardAppSKeyToNS +
                ", macVersion=" + macVersion +
                ", credential=" + credential +
                '}';
    }
}
