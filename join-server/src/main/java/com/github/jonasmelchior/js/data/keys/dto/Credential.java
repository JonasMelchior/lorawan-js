package com.github.jonasmelchior.js.data.keys.dto;

public class Credential {
    private String credentialID;
    private String password;

    public Credential() {
    }

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "credentialID='" + credentialID + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
