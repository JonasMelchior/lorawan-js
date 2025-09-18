package org.cibicom.iot.js.service.device;

import org.cibicom.iot.js.data.device.SessionStatus;

public interface ISessionStatusService {
    void save(SessionStatus sessionStatus);
    void delete(SessionStatus sessionStatus);
}
