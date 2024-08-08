package com.github.jonasmelchior.js.data.keys;

import com.github.jonasmelchior.js.data.user.User;
import jakarta.persistence.*;

@Entity(name = "key_credential")
public class KeyCredential {
    @Id
    private String identifier;
    private String credential;
    private String iv;
    @ManyToOne
    private User owner;

    public KeyCredential() {
    }

    public KeyCredential(String identifier, String credential) {
        this.identifier = identifier;
        this.credential = credential;
    }

    public KeyCredential(String identifier, String credential, User owner) {
        this.identifier = identifier;
        this.credential = credential;
        this.owner = owner;
    }

    public KeyCredential(String identifier, String credential, String iv, User owner) {
        this.identifier = identifier;
        this.credential = credential;
        this.iv = iv;
        this.owner = owner;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    @Override
    public String toString() {
        return this.identifier;
    }
}

