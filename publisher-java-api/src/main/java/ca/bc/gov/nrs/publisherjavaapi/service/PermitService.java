package ca.bc.gov.nrs.publisherjavaapi.service;

import ca.bc.gov.nrs.publisherjavaapi.model.Event;
import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import ca.bc.gov.nrs.publisherjavaapi.repository.EventRepository;
import ca.bc.gov.nrs.publisherjavaapi.repository.PermitRepo;
import ca.bc.gov.nrs.publisherjavaapi.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermitService {
  private final PermitRepo permitRepo;
  private final EventRepository eventRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<Permit, Event> createPermit(Permit permit) throws JsonProcessingException {
    var savedEntity = permitRepo.save(permit);
    var eventEntity = createEvent("PERMIT_CREATED", permit, savedEntity);
    eventRepository.save(eventEntity);
    return Pair.of(savedEntity, eventEntity);
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<Permit, Event> updatePermit(Permit permit) throws JsonProcessingException {
    val existingPermit = permitRepo.findById(permit.getPermitId());
    if (existingPermit.isPresent()) {
      val permitToUpdate = existingPermit.get();
      permitToUpdate.setPermitType(permit.getPermitType());
      permitToUpdate.setPermitArea(permit.getPermitArea());

      var eventEntity = createEvent("PERMIT_UPDATED", permit, permitToUpdate);
      permitRepo.save(permitToUpdate);
      eventRepository.save(eventEntity);
      return Pair.of(permitToUpdate, eventEntity);
    } else {
      throw new EntityNotFoundException("Permit not found");
    }
  }

  private static Event createEvent(String type, Permit permit, Permit savedEntity) throws JsonProcessingException {
    Event eventEntity = new Event();
    eventEntity.setType(type);
    eventEntity.setSource("PERMIT_API");
    eventEntity.setCreatedBy(permit.getCreatedBy());
    eventEntity.setUpdatedBy(permit.getUpdatedBy());
    eventEntity.setSubject("EVENTS-TOPIC");
    eventEntity.setPayloadVersion("1");
    eventEntity.setData(JsonUtil.getJsonStringFromObject(savedEntity));
    return eventEntity;
  }

  public Page<Permit> getPermits(Specification<Permit> permitSpecification, Integer pageNumber, Integer pageSize, final List<Sort.Order> sorts) {
    Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    return permitRepo.findAll(permitSpecification, paging);
  }
}
