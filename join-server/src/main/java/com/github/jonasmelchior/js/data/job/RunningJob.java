package com.github.jonasmelchior.js.data.job;

import com.github.jonasmelchior.js.data.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity(name = "running_job")
public class RunningJob {
    @Id
    @GeneratedValue
    private Long id;
    private JobType type;
    private LocalDateTime initializedAt;
    private Integer devicesNum;
    @ManyToOne
    private User owner;

    public RunningJob() {
    }

    public RunningJob(JobType type, LocalDateTime initializedAt, User owner) {
        this.type = type;
        this.initializedAt = initializedAt;
        this.owner = owner;
    }

    public RunningJob(JobType type, LocalDateTime initializedAt, User owner, Integer devicesNum) {
        this.type = type;
        this.initializedAt = initializedAt;
        this.owner = owner;
        this.devicesNum = devicesNum;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public LocalDateTime getInitializedAt() {
        return initializedAt;
    }

    public void setInitializedAt(LocalDateTime initializedAt) {
        this.initializedAt = initializedAt;
    }


    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Integer getDevicesNum() {
        return devicesNum;
    }

    public void setDevicesNum(Integer devicesNum) {
        this.devicesNum = devicesNum;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", type=" + type +
                ", initializedAt=" + initializedAt +
                ", user=" + owner +
                '}';
    }
}
