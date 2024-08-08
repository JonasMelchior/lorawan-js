package com.github.jonasmelchior.js.service.user;

import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService, UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findUserByEmail(email);
    }

    @Override
    public Optional<User> authorize(String email, String password) {
        Optional<User> user = findByEmail(email);
        if (user.isPresent()) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (bCryptPasswordEncoder.matches(password, user.get().getPasswordHash())) {
                return user;
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = repository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }
        else {
            List<String> roles = new ArrayList<>();
            roles.add("USER");
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.get().getEmail())
                    .password(user.get().getPasswordHash())
                    .roles(roles.toArray(new String[0]))
                    .build();
        }

    }
}
