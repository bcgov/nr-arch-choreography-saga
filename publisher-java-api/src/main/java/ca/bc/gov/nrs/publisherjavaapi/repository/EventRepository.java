package ca.bc.gov.nrs.publisherjavaapi.repository;

import ca.bc.gov.nrs.publisherjavaapi.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
}
