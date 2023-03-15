package main

import (
	"fmt"
	"github.com/nats-io/nats.go"
	"github.com/valyala/fasthttp"
	"os"
)

func HandleSubscriptionForExternalAPIAndNotifyUsingHttp(nc *nats.Conn) {
	js, _ := nc.JetStream()
	_, subErr := js.QueueSubscribe(getEnv("EXTERNAL_CONSUMER_TOPIC", "EVENTS-TOPIC"), getEnv("EXTERNAL_CONSUMER_Q_NAME", "external_consumer"), externalConsumerMessageHandler, nats.Durable(getEnv("EXTERNAL_CONSUMER_NAME", "external_consumer")))
	if subErr != nil {
		sysLog.Fatalf("Error: %v", subErr)
		os.Exit(127)
	}
}
func externalConsumerMessageHandler(msg *nats.Msg) {
	var meta, _ = msg.Metadata()

	var message = fmt.Sprintf("Received a message: seq[%d], pending[%d], data[%s]", meta.Sequence, meta.NumPending, string(msg.Data))

	req := fasthttp.AcquireRequest()
	res := fasthttp.AcquireResponse()
	defer fasthttp.ReleaseRequest(req)
	defer fasthttp.ReleaseResponse(res)
	req.SetRequestURI(getEnv("EXTERNAL_CONSUMER_API_URL", "http://localhost:8080/external-api/"))
	req.Header.SetMethod("POST")
	req.Header.SetContentType("application/json")
	req.Header.Add("X_API_KEY", getEnv("EXTERNAL_CONSUMER_API_KEY", "API_KEY"))
	req.SetBody(msg.Data)
	client := &fasthttp.Client{}
	client.Do(req, res)
	bodyBytes := res.Body()
	fmt.Println(string(bodyBytes))

	fmt.Println(message)
}
