package org.cibicom.iot.js.repository;

import org.cibicom.iot.js.data.job.RunningJob;
import org.cibicom.iot.js.data.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunningJobRepository extends JpaRepository<RunningJob, Long> {

    List<RunningJob> findAllByOwner(User owner);
}
