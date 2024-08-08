package com.github.jonasmelchior.js.data.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.json.Views;
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
    @JsonIgnore
    private String passwordHash;
    private UserType userType;

    public User() {
    }

    public User(String email, String password, UserType userType) {
        this.email = email;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
        this.userType = userType;
    }

    public User(String email, String firstName, String lastName, String password, UserType userType) {
        this.email = email;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        this.passwordHash = bCryptPasswordEncoder.encode(password);
        this.userType = userType;
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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

}
