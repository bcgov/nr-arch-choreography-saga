package main

import (
	"github.com/nats-io/nats.go"
	"github.com/sirupsen/logrus"
	"github.com/valyala/fasthttp"
	"os"
	"time"
)

func HandleSubscriptionForExternalAPIAndNotifyUsingHttp(nc *nats.Conn) {
	js, _ := nc.JetStream()
	// Create a consumer on the fly
	consumer, err := js.ConsumerInfo("EVENTS", getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer"))
	if err != nil {
		logrus.Error(err)
		return
	}
	if consumer != nil {
		consumer.Config.AckWait = 60 * time.Second
		consumer.Config.DeliverSubject = getEnv("EXTERNAL_CONSUMER_NAME", "EVENTS-DELIVER") + "-subject"
		consumer.Config.DeliverPolicy = nats.DeliverAllPolicy
		consumer.Config.AckPolicy = nats.AckExplicitPolicy
		consumer.Config.Durable = getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer")
		consumer.Config.ReplayPolicy = nats.ReplayInstantPolicy
		consumer.Config.MaxDeliver = 100
		js.UpdateConsumer("EVENTS", &consumer.Config)

	} else {
		consumerConfig := &nats.ConsumerConfig{
			AckWait:        60 * time.Second,
			DeliverSubject: getEnv("EXTERNAL_CONSUMER_NAME", "EVENTS-DELIVER") + "-subject",
			DeliverPolicy:  nats.DeliverAllPolicy,
			AckPolicy:      nats.AckExplicitPolicy,
			Durable:        getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer"),
			ReplayPolicy:   nats.ReplayInstantPolicy,
			MaxDeliver:     100,
		}
		consumer, _ = js.AddConsumer("EVENTS", consumerConfig)
	}
	_, subErr := js.QueueSubscribe("EVENTS-TOPIC", getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer"), externalConsumerMessageHandler, nats.Bind("EVENTS", getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer")))

	if subErr != nil {
		logrus.Fatalf("Error: %v", subErr)
		os.Exit(127)
	}
}
func externalConsumerMessageHandler(msg *nats.Msg) {
	req := fasthttp.AcquireRequest()
	res := fasthttp.AcquireResponse()
	defer fasthttp.ReleaseRequest(req)
	defer fasthttp.ReleaseResponse(res)
	req.SetRequestURI(getEnv("EXTERNAL_CONSUMER_API_URL", "http://localhost:8080/external-api/"))
	req.Header.SetMethod("POST")
	req.Header.SetContentType("application/json")
	req.Header.Add("X_API_KEY", getEnv("EXTERNAL_CONSUMER_API_KEY", "API_KEY"))
	req.SetBody(msg.Data)

	err := client.Do(req, res)
	if err != nil {
		logrus.Error(err)
		return
	}
	bodyBytes := res.Body()
	logrus.Info(string(bodyBytes))
	msg.Ack()
}
