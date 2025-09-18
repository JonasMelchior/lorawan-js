package org.cibicom.iot.js.service.utils;

import org.cibicom.iot.js.data.job.RunningJob;
import org.cibicom.iot.js.data.user.User;

import java.util.List;

public interface IRunningJobService {
    void save(RunningJob runningJob);
    void delete(RunningJob runningJob);
    List<RunningJob> findByOwner(User owner);
}
