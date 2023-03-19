package ca.bc.gov.nrs.publisherjavaapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "event")
@Getter
@Setter
@EqualsAndHashCode
@ToString

public class Event {
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @GeneratedValue(generator = "UUID")
  @Id
  private UUID id;
  private String type;
  private String source;
  private String payloadVersion;
  @JdbcTypeCode(SqlTypes.JSON)
  private String data; //json string
  private String subject;
  String createdBy;
  String updatedBy;
  @Column(name = "created_at")
  LocalDateTime createdAt;

  @Column(name = "updated_at")
  LocalDateTime updatedAt;

}
