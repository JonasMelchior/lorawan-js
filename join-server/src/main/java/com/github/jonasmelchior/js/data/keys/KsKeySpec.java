package com.github.jonasmelchior.js.data.keys;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "ks_key_spec")
public class KsKeySpec {
    @Id
    @GeneratedValue
    private Long id;
    private KeyType keyType;
    private String alias;

    public KsKeySpec() {
    }

    public KsKeySpec(KeyType keyType, String alias) {
        this.keyType = keyType;
        this.alias = alias;
    }

    public KsKeySpec(KeyType keyType) {
        this.keyType = keyType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
