package org.cibicom.iot.js.data.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.cibicom.iot.js.data.lrwan.MACVersion;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.json.Views;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "device")
@Table(indexes = @Index(name = "idx_deveui", columnList = "devEUI"))
public class Device implements RSQLVisitor {
    @JsonView(Views.Public.class) // Always included
    private String devEUI;
    @JsonView(Views.Public.class) // Always included
    private Boolean rootKeysExposed;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(Views.Detailed.class)
    private SessionStatus sessionStatus;
    @JsonView(Views.Detailed.class)
    private Boolean isKekEnabled = false;
    @JsonView(Views.Detailed.class)
    private String kekLabel;
    @JsonView(Views.Detailed.class)
    private Boolean forwardAppSKeyToNS = true;
    @ManyToOne
    @JsonView(Views.Detailed.class)
    private User owner;
    @JsonView(Views.Public.class) // Always included
    private MACVersion macVersion;
    @ManyToOne
    @JsonIgnore // Maybe to be used in the future
    private DeviceGroup groupId;
    @JsonView(Views.Public.class) // Always included
    private LocalDateTime lastJoin;
    @JsonView(Views.Public.class) // Always included
    private LocalDateTime createdAt;
    @JsonView(Views.Public.class) // Always included
    private LocalDateTime updatedAt;
    @Id
    @GeneratedValue
    private Long id;

    public Device() {
    }

    public Device(String devEUI, Boolean rootKeysExposed, SessionStatus sessionStatus, MACVersion macVersion) {
        this.devEUI = devEUI;
        this.rootKeysExposed = rootKeysExposed;
        this.sessionStatus = sessionStatus;
        this.macVersion = macVersion;
    }

    public Device(String devEUI, Boolean rootKeysExposed, SessionStatus sessionStatus, User owner, MACVersion macVersion) {
        this.devEUI = devEUI;
        this.rootKeysExposed = rootKeysExposed;
        this.sessionStatus = sessionStatus;
        this.owner = owner;
        this.macVersion = macVersion;
    }

    public Device(String devEUI, Boolean rootKeysExposed, SessionStatus sessionStatus, User owner, MACVersion macVersion, Boolean forwardAppSKeyToNS) {
        this.devEUI = devEUI;
        this.rootKeysExposed = rootKeysExposed;
        this.sessionStatus = sessionStatus;
        this.owner = owner;
        this.macVersion = macVersion;
        this.forwardAppSKeyToNS = forwardAppSKeyToNS;
    }

    public Device(String devEUI, Boolean rootKeysExposed, SessionStatus sessionStatus, User owner, MACVersion macVersion, DeviceGroup groupId) {
        this.devEUI = devEUI;
        this.rootKeysExposed = rootKeysExposed;
        this.sessionStatus = sessionStatus;
        this.owner = owner;
        this.macVersion = macVersion;
        this.groupId = groupId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(LocalDateTime lastJoin) {
        this.lastJoin = lastJoin;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public DeviceGroup getGroupId() {
        return groupId;
    }

    public void setGroupId(DeviceGroup groupId) {
        this.groupId = groupId;
    }

    public String getDevEUI() {
        return devEUI;
    }

    public void setDevEUI(String devEUI) {
        this.devEUI = devEUI;
    }

    public Boolean getRootKeysExposed() {
        return rootKeysExposed;
    }

    public void setRootKeysExposed(Boolean rootKeysExposed) {
        this.rootKeysExposed = rootKeysExposed;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public MACVersion getMacVersion() {
        return macVersion;
    }

    public void setMacVersion(MACVersion macVersion) {
        this.macVersion = macVersion;
    }

    @Override
    public Object visit(AndNode andNode, Object o) {
        return null;
    }

    @Override
    public Object visit(OrNode orNode, Object o) {
        return null;
    }

    @Override
    public Object visit(ComparisonNode comparisonNode, Object o) {
        return null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getKekLabel() {
        return kekLabel;
    }

    public void setKekLabel(String kekLabel) {
        this.kekLabel = kekLabel;
    }

    public Boolean getKekEnabled() {
        return isKekEnabled;
    }

    public void setKekEnabled(Boolean kekEnabled) {
        isKekEnabled = kekEnabled;
    }

    public Boolean getForwardAppSKeyToNS() {
        return forwardAppSKeyToNS;
    }

    public void setForwardAppSKeyToNS(Boolean forwardAppSKeyToNS) {
        this.forwardAppSKeyToNS = forwardAppSKeyToNS;
    }
}
