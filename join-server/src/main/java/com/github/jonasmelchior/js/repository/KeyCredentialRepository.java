package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeyCredentialRepository extends JpaRepository<KeyCredential, String> {
    Optional<KeyCredential> findByIdentifierAndOwner(String identifier, User owner);
    List<KeyCredential> findAllByOwner(User owner);
}
