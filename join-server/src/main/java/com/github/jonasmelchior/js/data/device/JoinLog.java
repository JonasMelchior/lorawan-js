package com.github.jonasmelchior.js.data.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.json.Views;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "device_join_log")
public class JoinLog {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JsonIgnore
    private Device device;
    @Column(length = 1024)
    @JsonView(Views.Public.class)
    private String joinReq;
    @Column(length = 1024)
    @JsonView(Views.Public.class)
    private String joinAns;
    @JsonView(Views.Public.class)
    private Boolean success;
    @JsonView(Views.Public.class)
    private LocalDateTime joinReqAttemptTime;

    public JoinLog() {
    }

    public JoinLog(Device device, String joinReq, LocalDateTime joinReqAttemptTime) {
        this.device = device;
        this.joinReq = joinReq;
        this.joinReqAttemptTime = joinReqAttemptTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getJoinReq() {
        return joinReq;
    }

    public void setJoinReq(String joinReq) {
        this.joinReq = joinReq;
    }

    public String getJoinAns() {
        return joinAns;
    }

    public void setJoinAns(String joinAns) {
        this.joinAns = joinAns;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getJoinReqAttemptTime() {
        return joinReqAttemptTime;
    }

    public void setJoinReqAttemptTime(LocalDateTime joinReqAttemptTime) {
        this.joinReqAttemptTime = joinReqAttemptTime;
    }
}
