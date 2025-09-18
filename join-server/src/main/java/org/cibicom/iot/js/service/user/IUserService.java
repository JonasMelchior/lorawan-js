package org.cibicom.iot.js.service.user;

import org.cibicom.iot.js.data.user.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<User> findAll();
    Optional<User> findByEmail(String email);
    Optional<User> authorize(String email, String password);
    void save(User user);
    void delete(User user);
}
