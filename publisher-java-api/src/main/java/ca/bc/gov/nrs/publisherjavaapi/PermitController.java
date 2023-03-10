package ca.bc.gov.nrs.publisherjavaapi;

import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import ca.bc.gov.nrs.publisherjavaapi.service.PermitService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permit")
@RequiredArgsConstructor
public class PermitController {
  private final PermitService permitService;

  @PostMapping
  public PermitDTO createPermit(@Validated @RequestBody PermitDTO permitDTO) {
    val permit = permitService.createPermit(permitDTO.toPermit());
    return new PermitDTO(permit.getPermitId(), permit.getPermitType(), permit.getPermitArea());
  }

  @PutMapping
  public PermitDTO updatePermit(@Validated @RequestBody PermitDTO permitDTO) {
    val permit = permitService.updatePermit(permitDTO.toPermit());
    return new PermitDTO(permit.getPermitId(), permit.getPermitType(), permit.getPermitArea());
  }

  public record PermitDTO(UUID permitId, String permitType, String permitArea) {
    @SneakyThrows
    public Permit toPermit() {
      Permit permit = new Permit();
      permit.setPermitId(permitId);
      permit.setPermitType(permitType);
      permit.setPermitArea(permitArea);
      return permit;
    }
  }
}
