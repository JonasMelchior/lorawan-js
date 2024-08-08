package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.keys.DevKeyId;
import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.repository.DevKeyIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DevKeyIdService implements IDevKeyIdService {
    @Autowired
    private DevKeyIdRepository repository;

    @Override
    public Optional<DevKeyId> findByDevEUI(String devEUI) {
        return repository.findByDevEUI(devEUI);
    }

    @Override
    public Optional<KeyCredential> findCredentialByDevEUI(String devEUI) {
        return repository.findByDevEUI(devEUI)
                .map(DevKeyId::getKeyCredential);
    }

    public void save(DevKeyId devKeyId) {
        repository.save(devKeyId);
    }

    @Override
    public void saveAll(Set<DevKeyId> devKeyIds) {
        repository.saveAll(devKeyIds);
    }

    public List<DevKeyId> findAllByDevEUI(List<String> devEUIs) {
        return repository.findAllByDevEUIIn(devEUIs);
    }

    @Override
    public void delete(DevKeyId devKeyId) {
        repository.delete(devKeyId);
    }

    @Override
    public void deleteAll(List<DevKeyId> devKeyIds) {
        repository.deleteAll(devKeyIds);
    }
}
