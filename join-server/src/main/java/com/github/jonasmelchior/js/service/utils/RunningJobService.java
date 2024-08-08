package com.github.jonasmelchior.js.service.utils;

import com.github.jonasmelchior.js.data.job.RunningJob;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.repository.RunningJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunningJobService implements IRunningJobService {

    @Autowired
    private RunningJobRepository repository;

    @Override
    public void save(RunningJob runningJob) {
        repository.save(runningJob);
    }

    @Override
    public void delete(RunningJob runningJob) {
        repository.delete(runningJob);
    }

    @Override
    public List<RunningJob> findByOwner(User owner) {
        return repository.findAllByOwner(owner);
    }
}
