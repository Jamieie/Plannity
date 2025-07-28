package org.mi.plannitybe.schedule.repository;

import org.mi.plannitybe.schedule.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
