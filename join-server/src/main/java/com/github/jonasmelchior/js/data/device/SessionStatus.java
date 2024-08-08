package com.github.jonasmelchior.js.data.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.json.Views;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "session_status")
@JsonView(Views.Detailed.class)
public class SessionStatus {
    @JsonIgnore
    private String devEUI;
    private String devAddr;
    private String lastDevNonce;
    private String lastJoinNonce;
    private String sessionKeyId;
    private Integer sessionNum;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> usedDevNonces = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> usedJoinNonces = new ArrayList<>();
    private SessionState state;
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    public SessionStatus(String devAddr, String lastDevNonce, String lastJoinNonce, String sessionKeyId, Integer sessionNum) {
        this.devAddr = devAddr;
        this.lastDevNonce = lastDevNonce;
        this.lastJoinNonce = lastJoinNonce;
        this.sessionKeyId = sessionKeyId;
        this.sessionNum = sessionNum;
    }

    public SessionStatus(String devAddr, String lastDevNonce, String lastJoinNonce, String sessionKeyId, Integer sessionNum, List<String> usedDevNonces, List<String> usedJoinNonces) {
        this.devAddr = devAddr;
        this.lastDevNonce = lastDevNonce;
        this.lastJoinNonce = lastJoinNonce;
        this.sessionKeyId = sessionKeyId;
        this.sessionNum = sessionNum;
        this.usedDevNonces = usedDevNonces;
        this.usedJoinNonces = usedJoinNonces;
    }

    public SessionStatus(String devAddr, String lastDevNonce, String lastJoinNonce, String sessionKeyId, Integer sessionNum, List<String> usedDevNonces, List<String> usedJoinNonces, SessionState state) {
        this.devAddr = devAddr;
        this.lastDevNonce = lastDevNonce;
        this.lastJoinNonce = lastJoinNonce;
        this.sessionKeyId = sessionKeyId;
        this.sessionNum = sessionNum;
        this.usedDevNonces = usedDevNonces;
        this.usedJoinNonces = usedJoinNonces;
        this.state = state;
    }

    public SessionStatus(String devEUI, String devAddr, String lastDevNonce, String lastJoinNonce, String sessionKeyId, Integer sessionNum, List<String> usedDevNonces, List<String> usedJoinNonces, SessionState state) {
        this.devEUI = devEUI;
        this.devAddr = devAddr;
        this.lastDevNonce = lastDevNonce;
        this.lastJoinNonce = lastJoinNonce;
        this.sessionKeyId = sessionKeyId;
        this.sessionNum = sessionNum;
        this.usedDevNonces = usedDevNonces;
        this.usedJoinNonces = usedJoinNonces;
        this.state = state;
    }

    public SessionStatus() {
    }


    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public String getDevAddr() {
        return devAddr;
    }

    public void setDevAddr(String devAddr) {
        this.devAddr = devAddr;
    }

    public String getLastDevNonce() {
        return lastDevNonce;
    }

    public void setLastDevNonce(String lastDevNonce) {
        this.lastDevNonce = lastDevNonce;
    }

    public String getLastJoinNonce() {
        return lastJoinNonce;
    }

    public void setLastJoinNonce(String lastJoinNonce) {
        this.lastJoinNonce = lastJoinNonce;
    }

    public String getSessionKeyId() {
        return sessionKeyId;
    }

    public void setSessionKeyId(String sessionKeyId) {
        this.sessionKeyId = sessionKeyId;
    }

    public Integer getSessionNum() {
        return sessionNum;
    }

    public void setSessionNum(Integer sessionNum) {
        this.sessionNum = sessionNum;
    }

    public List<String> getUsedDevNonces() {
        return usedDevNonces;
    }

    public void setUsedDevNonces(List<String> usedDevNonces) {
        this.usedDevNonces = usedDevNonces;
    }

    public List<String> getUsedJoinNonces() {
        return usedJoinNonces;
    }

    public void setUsedJoinNonces(List<String> usedJoinNonces) {
        this.usedJoinNonces = usedJoinNonces;
    }

    public static SessionStatus sessionStatusInit() {
        return new SessionStatus(
                "N/A",
                "000000",
                "000000",
                "0",
                0,
                new ArrayList<>() {{
                    add("000000");
                }},
                new ArrayList<>() {{
                    add("000000");
                }},
                SessionState.INIT
        );
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
