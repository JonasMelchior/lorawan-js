package org.cibicom.iot.js.data.user;

public enum UserType {
    USER,
    ADMIN;

    @Override
    public String toString() {
        return this.name();
    }
}
