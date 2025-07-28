package org.mi.plannitybe.schedule.repository;

import org.mi.plannitybe.schedule.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}
