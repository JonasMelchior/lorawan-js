package com.github.jonasmelchior.js.data.keys;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class KeySpec {
    @JsonIgnore
    private String devEUI;
    private String key;
    private KeyType keyType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeySpec keySpec = (KeySpec) o;
        return Objects.equals(devEUI, keySpec.devEUI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devEUI);
    }

    public KeySpec(String devEUI, String key, KeyType keyType) {
        this.devEUI = devEUI;
        this.key = key;
        this.keyType = keyType;
    }

    public KeySpec(String key, KeyType keyType) {
        this.key = key;
        this.keyType = keyType;
    }

    public KeySpec() {
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    @Override
    public String toString() {
        return "KeySpec{" +
                "devEUI='" + devEUI + '\'' +
                ", key='" + key + '\'' +
                ", keyType=" + keyType +
                '}';
    }
}
