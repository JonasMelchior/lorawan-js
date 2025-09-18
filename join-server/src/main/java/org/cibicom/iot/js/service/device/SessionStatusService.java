package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.device.SessionStatus;
import org.cibicom.iot.js.repository.SessionStatusRepository;
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
