package com.github.jonasmelchior.js.repository;

import com.github.jonasmelchior.js.data.job.RunningJob;
import com.github.jonasmelchior.js.data.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunningJobRepository extends JpaRepository<RunningJob, Long> {

    List<RunningJob> findAllByOwner(User owner);
}
