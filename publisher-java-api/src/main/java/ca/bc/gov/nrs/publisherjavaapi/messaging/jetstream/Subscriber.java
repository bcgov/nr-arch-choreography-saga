package ca.bc.gov.nrs.publisherjavaapi.messaging.jetstream;


import ca.bc.gov.nrs.publisherjavaapi.properties.ApplicationProperties;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
  private final Connection natsConnection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection the nats connection
   */
  @Autowired
  public Subscriber(final Connection natsConnection) {
    this.natsConnection = natsConnection;
  }


  /**
   * This subscription will make sure the messages are required to acknowledge manually to Jet Stream.
   * Subscribe.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = "EVENTS-TOPIC-PUBLISHER-API";
    val autoAck = false;
    PushSubscribeOptions options = PushSubscribeOptions.builder()
      .stream(ApplicationProperties.STREAM_NAME)
      .name("EVENTS-TOPIC-PUBLISHER-API")
      .deliverSubject("EVENTS-TOPIC-PUBLISHER-API")
      .durable("EVENTS-TOPIC-PUBLISHER-API")
      .configuration(ConsumerConfiguration.builder()
        .deliverPolicy(DeliverPolicy.New).ackPolicy(AckPolicy.Explicit)
        .build()).build();
    this.natsConnection.jetStream().subscribe("EVENTS-TOPIC", qName, this.natsConnection.createDispatcher(), this::onEventsTopicMessage,
      autoAck, options);
  }

  /**
   * This method will process the event message pushed into the student_events_topic.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link Event} if it not types of event then it will throw exception and will be ignored.
   */
  public void onEventsTopicMessage(final Message message) {
    log.info("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
    try {
      val eventString = new String(message.getData());
      /*LogHelper.logMessagingEventDetails(eventString);
      ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
      jetStreamEventHandlerService.updateEventStatus(event);
      log.info("received event :: {} ", event); */
      log.info("received event :: {} ", eventString);
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }

}
