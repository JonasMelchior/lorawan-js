package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.repository.KeyCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KeyCredentialService implements IKeyCredentialService{

    @Autowired
    private KeyCredentialRepository repository;

    @Override
    public Optional<KeyCredential> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<KeyCredential> findByIdentifierAndByOwner(String identifier, User owner) {
        return repository.findByIdentifierAndOwner(identifier, owner);
    }

    @Override
    public List<KeyCredential> findByOwner(User owner) {
        return repository.findAllByOwner(owner);
    }


    @Override
    public void save(KeyCredential keyCredential) {
        repository.save(keyCredential);
    }

    @Override
    public void delete(KeyCredential keyCredential) {
        repository.delete(keyCredential);
    }
}
