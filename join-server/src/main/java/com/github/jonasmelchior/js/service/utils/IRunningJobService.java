package com.github.jonasmelchior.js.service.utils;

import com.github.jonasmelchior.js.data.job.RunningJob;
import com.github.jonasmelchior.js.data.user.User;

import java.util.List;

public interface IRunningJobService {
    void save(RunningJob runningJob);
    void delete(RunningJob runningJob);
    List<RunningJob> findByOwner(User owner);
}
