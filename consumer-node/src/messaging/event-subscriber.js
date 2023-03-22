'use strict';
const TOPICS = ['EVENTS-TOPIC'];
const logger = require('../logger');
const NATS = require('./nats-con');
const emailHelper = require('../email');
const {AckPolicy, DeliverPolicy, StringCodec, createInbox, consumerOpts} = require('nats');


function generateHtmlBody(data) {
  return `<!DOCTYPE html>
<html>
  <head>
    <title>JSON Data Table</title>
    <style>
      table,
      th,
      td {
        border: 1px solid black;
        border-collapse: collapse;
        padding: 5px;
      }
    </style>
  </head>
  <body>
    <table>
      <thead>
        <tr>
        ${Object.keys(data).map((key) => `<th>${key}</th>`).join('')}
        </tr>
      </thead>
      <tbody>
        <tr>
         ${Object.values(data).map((value) => `<td>${value}</td>`).join('')}
        </tr>
        
      </tbody>
    </table>
  </body>
</html>
`;
}

const handleJetStreamMessage = async (err, msg) => {
  if (err) {
    logger.error(err);
    return;
  }
  try {
    const data = JSON.parse(StringCodec().decode(msg.data)); // it would always be a JSON string. it will always be choreographed event.
    logger.info(`Received message, on ${msg.subject} , Sequence ::  [${msg.seq}], sid ::  [${msg.sid}], redelivered ::  [${msg.redelivered}] :: Data ::`, data);
    const email = {
      bodyType: 'html',
      body: generateHtmlBody(data),
      delayTS: 0,
      encoding: 'utf-8',
      from: 'omprakash.2.mishra@gov.bc.ca',
      priority: 'normal',
      subject: 'Event Received',
      to: ['omprakash.2.mishra@gov.bc.ca']
    };

    await emailHelper.send(email);
    logger.info('Email sent successfully');
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
      deliver_group: 'consumer-node-api',
      name: 'consumer-node-api'
    };
    const opts = consumerOpts(config);
    opts.stream = 'EVENTS';
    opts.durable('consumer-node-api');
    opts.manualAck();
    opts.ackExplicit();
    opts.deliverNew();
    opts.deliverGroup('consumer-node-api');
    opts.deliverTo(createInbox('consumer-node-api'));

    let sub = await jetStream.subscribe(key, opts);
    const done = (async () => {
      for await (const m of sub) {
        await handleJetStreamMessage(null, m);
        m.ack();
      }
    })();
  });

};
module.exports = {
  subscribe
};
