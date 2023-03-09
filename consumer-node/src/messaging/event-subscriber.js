'use strict';
const TOPICS = ['EVENTS-TOPIC'];
const logger = require('../logger');
const NATS = require('./nats-con');
const {AckPolicy, DeliverPolicy, StringCodec, createInbox, consumerOpts} = require('nats');


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
    logger.error('Error while handling student data from update student event', e);
    msg.ack(); // acknowledge to JetStream
  }
};

const subscribe = () => {
  const jetStream = NATS.getConnection().jetstream();
  TOPICS.forEach(async (key) => {

    const opts = consumerOpts();
    opts.durable("consumer-node-api");
    opts.manualAck();
    opts.ackExplicit();
    opts.deliverTo(createInbox('consumer-node-api'));

    let sub = await jetStream.subscribe(key, opts);
    const done = (async () => {
      for await (const m of sub) {
        logger.info(`Received message, on ${m.subject} , Sequence ::  [${m.seq}], sid ::  [${m.sid}], redelivered ::  [${m.redelivered}] :: Data ::`, m.data);
        await handleJetStreamMessage(null, m);
        m.ack();
      }
    })();

    /*const consumerOpts = {
      config: {
        name: 'consumer-node-api',
        durable_name: 'consumer-node-api',
        ack_policy: AckPolicy.Explicit,
        deliver_policy: DeliverPolicy.New,
        deliver_subject: 'consumer-node-api'
      },
      mack: true,
      queue: 'consumer-node-api-queue-group',
      stream: 'EVENTS',
      callbackFn: handleJetStreamMessage,
    };
    await jetStream.subscribe(key, consumerOpts);*/
  });

};
module.exports = {
  subscribe
};
