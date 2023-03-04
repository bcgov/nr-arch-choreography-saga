'use strict';
const TOPICS = ['EVENTS_TOPIC'];
const logger = require('../logger');
const NATS = require('./nats-con');
const {AckPolicy, DeliverPolicy, StringCodec, createInbox, consumerOpts} = require('nats');


const handleJetStreamMessage = async (err, msg) => {
  if (err) {
    logger.error(err);
    return;
  }
  const data = JSON.parse(StringCodec().decode(msg.data)); // it would always be a JSON string. ii will always be choreographed event.
  logger.info(`Received message, on ${msg.subject} , Sequence ::  [${msg.seq}], sid ::  [${msg.sid}], redelivered ::  [${msg.redelivered}] :: Data ::`, data);
  try {
    msg.ack(); // acknowledge to JetStream
  } catch (e) {
    logger.error('Error while handling student data from update student event', e);
  }
};

const subscribe = () => {
  const jetStream = NATS.getConnection().jetstream();
  TOPICS.forEach(async (key) => {

    const opts = consumerOpts();
    opts.name="consumer-frontend-api";
    opts.queue="consumer-frontend-api-queue";
    opts.durable("consumer-frontend-api-durable");
    opts.manualAck();
    opts.ackExplicit();
    opts.deliverTo(createInbox("consumer-frontend-api"));
    opts.deliver_policy=process.env.DELIVER_POLICY || DeliverPolicy.New;
    opts.callback(handleJetStreamMessage);
    opts.stream="EVENTS";
    opts.ack_policy=process.env.ACK_POLICY || AckPolicy.Explicit;
    await jetStream.subscribe(key, opts);
  });

};
module.exports = {
  subscribe
};
