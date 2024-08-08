package com.github.jonasmelchior.js.service.device;

import com.github.jonasmelchior.js.data.device.SessionStatus;

public interface ISessionStatusService {
    void save(SessionStatus sessionStatus);
    void delete(SessionStatus sessionStatus);
}
