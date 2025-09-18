package org.cibicom.iot.js.data.keys;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class KeySpec {
    @JsonIgnore
    private String identifier;
    private String key;
    private KeyType keyType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeySpec keySpec = (KeySpec) o;
        return Objects.equals(identifier, keySpec.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    public KeySpec(String identifier, String key, KeyType keyType) {
        this.identifier = identifier;
        this.key = key;
        this.keyType = keyType;
    }

    public KeySpec(String key, KeyType keyType) {
        this.key = key;
        this.keyType = keyType;
    }

    public KeySpec() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
                "devEUI='" + identifier + '\'' +
                ", key='" + key + '\'' +
                ", keyType=" + keyType +
                '}';
    }
}
