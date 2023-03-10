package ca.bc.gov.nrs.publisherjavaapi.service;

import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import ca.bc.gov.nrs.publisherjavaapi.repository.PermitRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermitService {
  private final PermitRepo permitRepo;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Permit createPermit(Permit permit) {
    return permitRepo.save(permit);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Permit updatePermit(Permit permit) {
    val existingPermit = permitRepo.findById(permit.getPermitId());
    if (existingPermit.isPresent()) {
      val permitToUpdate = existingPermit.get();
      permitToUpdate.setPermitType(permit.getPermitType());
      permitToUpdate.setPermitArea(permit.getPermitArea());
      return permitRepo.save(permitToUpdate);
    } else {
      throw new EntityNotFoundException("Permit not found");
    }
  }
}
