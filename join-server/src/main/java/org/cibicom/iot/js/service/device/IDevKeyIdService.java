package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.keys.DevKeyId;
import org.cibicom.iot.js.data.keys.KeyCredential;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IDevKeyIdService {
    Optional<DevKeyId> findByDevEUI(String devEUI);
    // Might be deprecated
    Optional<KeyCredential> findCredentialByDevEUI(String devEUI);
    void save(DevKeyId devKeyId);
    void saveAll(Set<DevKeyId> devKeyIds);
    List<DevKeyId> findAllByDevEUI(List<String> devEUI);
    void delete(DevKeyId devKeyId);
    void deleteAll(List<DevKeyId> devKeyIds);
}
