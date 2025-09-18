package org.cibicom.iot.js.data.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.cibicom.iot.js.json.Views;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity(name = "js_user")
@JsonView(Views.Detailed.class)
public class User {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String organization;
    @JsonIgnore
    private String passwordHash;
    private UserType userType;
    @JsonIgnore
    private String activationToken;
    @JsonIgnore
    private Boolean active;
    @JsonIgnore
    private String resetToken;
    private Boolean isNsGatewayAdmin;


    public User() {
    }

    public User(String email, String password, UserType userType) {
        this.email = email;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
        this.userType = userType;
    }

    public User(String email, UserType userType) {
        this.email = email;
        this.userType = userType;
    }

    public User(String email, String firstName, String lastName, String password, UserType userType) {
        this.email = email;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
        this.userType = userType;
    }

    public User(String email, String firstName, String lastName, String password, UserType userType, String organization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
        this.userType = userType;
        this.organization = organization;
    }

    public User(String email, String firstName, String lastName, String organization) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
    }

    public void setPasswordHashHashed(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
