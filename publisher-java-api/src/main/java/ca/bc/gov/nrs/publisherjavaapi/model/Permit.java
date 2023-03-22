package ca.bc.gov.nrs.publisherjavaapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.postgresql.geometric.PGpolygon;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "permit")
@Data
public class Permit {
  @Column(name = "permit_id", columnDefinition = "uuid", nullable = false)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @GeneratedValue(generator = "UUID")
  @Id
  UUID permitId;
  String permitType;
  @Column(name = "permit_area", nullable = false)
  String permitArea;
  String createdBy;
  String updatedBy;
  @Column(name = "created_at")
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  LocalDateTime createdAt;

  @Column(name = "updated_at")
  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
  LocalDateTime updatedAt;
  @Column(name = "permit_lat_long")
  String permitLatLong;

}
