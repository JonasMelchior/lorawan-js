package com.github.jonasmelchior.js.data.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.json.Views;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "appskey_req_log")
public class AppSKeyReqLog {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JsonIgnore
    private Device device;
    @Column(length = 1024)
    @JsonView(Views.Public.class)
    private String appSKeyReq;
    @Column(length = 1024)
    @JsonView(Views.Public.class)
    private String appSKeyAns;
    @JsonView(Views.Public.class)
    private Boolean success;
    @JsonView(Views.Public.class)
    private LocalDateTime appSKeyReqAttemptTime;

    public AppSKeyReqLog(Device device, String appSKeyReq, LocalDateTime appSKeyReqAttemptTime) {
        this.device = device;
        this.appSKeyReq = appSKeyReq;
        this.appSKeyReqAttemptTime = appSKeyReqAttemptTime;
    }

    public AppSKeyReqLog() {

    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getAppSKeyReq() {
        return appSKeyReq;
    }

    public void setAppSKeyReq(String appSKeyReq) {
        this.appSKeyReq = appSKeyReq;
    }

    public String getAppSKeyAns() {
        return appSKeyAns;
    }

    public void setAppSKeyAns(String appSKeyReqAns) {
        this.appSKeyAns = appSKeyReqAns;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getAppSKeyReqAttemptTime() {
        return appSKeyReqAttemptTime;
    }

    public void setAppSKeyReqAttemptTime(LocalDateTime appSKeyReqAttemptTime) {
        this.appSKeyReqAttemptTime = appSKeyReqAttemptTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
