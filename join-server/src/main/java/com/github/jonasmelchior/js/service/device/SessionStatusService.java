package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.device.SessionStatus;
import com.github.jonasmelchior.js.repository.SessionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionStatusService implements ISessionStatusService{

    @Autowired
    private SessionStatusRepository repository;
    @Override
    public void save(SessionStatus sessionStatus) {
        repository.save(sessionStatus);
    }

    @Override
    public void delete(SessionStatus sessionStatus) {
        repository.delete(sessionStatus);
    }
}
