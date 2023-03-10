package ca.bc.gov.nrs.publisherjavaapi.properties;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class holds all application properties
 *
 * @author Marco Villeneuve
 */
@Component
@Getter
public class ApplicationProperties {

  /**
   * The constant STUDENT_API.
   */
  public static final String STREAM_NAME="EVENTS";
  public static final String CORRELATION_ID = "correlationID";
  /**
   * The Stan url.
   */
  @Value("${nats.url}")
  String natsUrl;


  @Value("${nats.maxReconnect}")
  Integer natsMaxReconnect;
}
