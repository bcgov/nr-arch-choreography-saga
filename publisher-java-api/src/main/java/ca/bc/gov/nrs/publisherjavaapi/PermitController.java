package ca.bc.gov.nrs.publisherjavaapi;

import ca.bc.gov.nrs.publisherjavaapi.messaging.jetstream.Publisher;
import ca.bc.gov.nrs.publisherjavaapi.model.Permit;
import ca.bc.gov.nrs.publisherjavaapi.service.PermitService;
import ca.bc.gov.nrs.publisherjavaapi.service.SearchService;
import ca.bc.gov.nrs.publisherjavaapi.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.JetStreamApiException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permit")
@RequiredArgsConstructor
public class PermitController {
  private final PermitService permitService;
  private final Publisher publisher;

  private final SearchService searchService;
  public static final ObjectMapper mapper = new ObjectMapper();

  @PostMapping
  public PermitDTO createPermit(@Validated @RequestBody PermitDTO permitDTO) throws JetStreamApiException, IOException {
    var permitPair = permitService.createPermit(PermitDTO.toPermit(permitDTO));
    publisher.publish(JsonUtil.getJsonStringFromObject(permitPair.getSecond()));
    var permit = permitPair.getFirst();
    return PermitDTO.toDTO(permit);
  }

  @PutMapping
  public PermitDTO updatePermit(@Validated @RequestBody PermitDTO permitDTO) throws IOException, JetStreamApiException {
    val permitPair = permitService.updatePermit(PermitDTO.toPermit(permitDTO));
    publisher.publish(JsonUtil.getJsonStringFromObject(permitPair.getSecond()));
    var permit = permitPair.getFirst();
    return PermitDTO.toDTO(permit);
  }

  @GetMapping("/paginated")
  public ResponseEntity<?> getPermits(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                      @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                      @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson) throws IOException {
    if (pageSize > 2000) {
      return ResponseEntity.badRequest().body("Page size cannot be greater than 2000");
    }
    final List<Sort.Order> sorts = new ArrayList<>();
    var specification = searchService.setSpecificationAndSortCriteria(sortCriteriaJson, searchCriteriaListJson, mapper, sorts);
    return ResponseEntity.ok(permitService.getPermits(specification, pageNumber, pageSize, sorts));
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record PermitDTO(UUID permitId, String permitType, String permitArea, String createdBy, String updatedBy) {

    public static Permit toPermit(PermitDTO permitDTO) {
      Permit permit = new Permit();
      permit.setPermitId(permitDTO.permitId());
      permit.setPermitType(permitDTO.permitType());
      permit.setPermitArea(permitDTO.permitArea());
      permit.setCreatedBy(permitDTO.createdBy());
      permit.setUpdatedBy(permitDTO.updatedBy());
      return permit;
    }

    public static PermitDTO toDTO(Permit permit) {
      return new PermitDTO(permit.getPermitId(), permit.getPermitType(), permit.getPermitArea(), permit.getCreatedBy(), permit.getUpdatedBy());
    }
  }
}
