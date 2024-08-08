package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.user.User;

import java.util.List;
import java.util.Optional;

public interface IKeyCredentialService {
    Optional<KeyCredential> findById(String id);
    Optional<KeyCredential> findByIdentifierAndByOwner(String identifier, User owner);
    List<KeyCredential> findByOwner(User owner);
    void save(KeyCredential keyCredential);
    void delete(KeyCredential keyCredential);
}
