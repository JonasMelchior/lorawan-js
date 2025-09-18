package org.cibicom.iot.js.data.keys;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity(name = "device_key_id")
@Table(indexes = @Index(name = "idx_deveui_keyid", columnList = "devEUI"))
public class DevKeyId {
    private String devEUI;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<KsKeySpec> ksKeySpecs;
    @ManyToOne
    private KeyCredential keyCredential;
    @Id
    @GeneratedValue
    private Long id;

    public DevKeyId() {
    }

    public DevKeyId(String devEUI, KeyCredential keyCredential) {
        this.devEUI = devEUI;
        this.keyCredential = keyCredential;
    }

    public DevKeyId(String devEUI, List<KsKeySpec> ksKeySpecs) {
        this.devEUI = devEUI;
        this.ksKeySpecs = ksKeySpecs;
    }

    public DevKeyId(String devEUI) {
        this.devEUI = devEUI;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }
    public KeyCredential getKeyCredential() {
        return keyCredential;
    }
    public void setKeyCredential(KeyCredential keyCredential) {
        this.keyCredential = keyCredential;
    }

    public List<KsKeySpec> getKsKeySpecs() {
        return ksKeySpecs;
    }

    public void setKsKeySpecs(List<KsKeySpec> ksKeySpecs) {
        this.ksKeySpecs = ksKeySpecs;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DevKeyId)) {
            return false;
        }
        DevKeyId other = (DevKeyId) o;
        return Objects.equals(devEUI, other.devEUI) && devEUI != null;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
