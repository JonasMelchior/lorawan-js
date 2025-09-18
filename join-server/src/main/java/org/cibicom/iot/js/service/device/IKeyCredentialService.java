package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.keys.KeyCredential;
import org.cibicom.iot.js.data.user.User;

import java.util.List;
import java.util.Optional;

public interface IKeyCredentialService {
    Optional<KeyCredential> findById(String id);
    Optional<KeyCredential> findByIdentifierAndByOwner(String identifier, User owner);
    List<KeyCredential> findByOwner(User owner);
    void save(KeyCredential keyCredential);
    void delete(KeyCredential keyCredential);
}
