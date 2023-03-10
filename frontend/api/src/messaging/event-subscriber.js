'use strict';
const TOPICS = ['EVENTS-TOPIC'];
const logger = require('../logger');
const NATS = require('./nats-con');
const {StringCodec, createInbox, consumerOpts, AckPolicy, DeliverPolicy} = require('nats');


const handleJetStreamMessage = async (err, msg) => {
  if (err) {
    logger.error(err);
    return;
  }
  try {
    const data = JSON.parse(StringCodec().decode(msg.data)); // it would always be a JSON string. ii will always be choreographed event.
    logger.info(`Received message, on ${msg.subject} , Sequence ::  [${msg.seq}], sid ::  [${msg.sid}], redelivered ::  [${msg.redelivered}] :: Data ::`, data);
    logger.info(data);

    msg.ack(); // acknowledge to JetStream
  } catch (e) {
    logger.error('Error while handling data from event', e);
    msg.ack(); // acknowledge to JetStream
  }
};

const subscribe = () => {
  const jetStream = NATS.getConnection().jetstream();
  TOPICS.forEach(async (key) => {
    const config = {
      deliver_policy: DeliverPolicy.New,
      ack_policy: AckPolicy.Explicit,
      deliver_group: 'consumer-frontend-api',
      name: 'consumer-frontend-api'
    };
    const opts = consumerOpts(config);
    opts.stream='EVENTS';
    opts.durable('consumer-frontend-api');
    opts.manualAck();
    opts.ackExplicit();
    opts.deliverNew();
    opts.deliverGroup('consumer-frontend-api');
    opts.deliverTo(createInbox('consumer-frontend-api'));

    let sub = await jetStream.subscribe(key, opts);
    const done = (async () => {
      for await (const m of sub) {
        logger.info(`Received message, on ${m.subject} , Sequence ::  [${m.seq}], sid ::  [${m.sid}], redelivered ::  [${m.redelivered}] :: Data ::`, m.data);
        await handleJetStreamMessage(null, m);
        m.ack();
      }
    })();
  });

};
module.exports = {
  subscribe
};
