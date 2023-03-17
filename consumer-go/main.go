package main

import (
	"bytes"
	"fmt"
	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/csrf"
	"github.com/gofiber/fiber/v2/middleware/favicon"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/gofiber/fiber/v2/middleware/recover"
	"github.com/gofiber/helmet/v2"
	"github.com/joho/godotenv"
	"github.com/nats-io/nats.go"
	"github.com/sirupsen/logrus"
	"github.com/valyala/fasthttp"
	"os"
	"time"
)

var (
	buf    bytes.Buffer
	client *fasthttp.Client = nil
)

func main() {
	_ = godotenv.Load()
	logrus.SetFormatter(&logrus.TextFormatter{
		ForceColors: true,
	})
	logrus.SetLevel(logrus.DebugLevel)
	app := fiber.New(fiber.Config{})
	app.Use(helmet.New())
	app.Use(favicon.New())
	app.Use(recover.New())
	app.Use(cors.New())
	app.Use(csrf.New())
	app.Get("/", func(c *fiber.Ctx) error {
		return c.SendString("Hello, World!")
	})
	app.Use(logger.New(logger.Config{
		TimeFormat: "2006-01-02T15:04:05",
		TimeZone:   "GMT",
	}))
	getHttpClient() // initialize http client
	nc, err := nats.Connect(getEnv("NATS_URL", "nats://localhost:4222"))
	if err != nil {
		logrus.Fatalf("Error: %v", err)
		os.Exit(127)
	}
	js, _ := nc.JetStream()
	_, subErr := js.QueueSubscribe("EVENTS-TOPIC", "CONSUMER-GO", handler, nats.Durable("CONSUMER-GO"))

	if subErr != nil {
		logrus.Fatalf("Error: %v", subErr)
		os.Exit(127)
	}
	HandleSubscriptionForExternalAPIAndNotifyUsingHttp(nc)
	//convert to number from string
	PORT := getEnv("PORT", "3000")
	var port = fmt.Sprintf(":%s", PORT)
	appErr := app.Listen(port)
	if appErr != nil {
		logrus.Fatalf("Error: %v", appErr)
		os.Exit(127)
	}

}

func handler(msg *nats.Msg) {
	var meta, _ = msg.Metadata()

	var message = fmt.Sprintf("Received a message: seq[%d], pending[%d], data[%s]", meta.Sequence, meta.NumPending, string(msg.Data))

	logrus.Info(message)
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func getHttpClient() *fasthttp.Client {
	if client != nil {
		return client
	}
	duration := 30 * time.Second

	client = &fasthttp.Client{NoDefaultUserAgentHeader: true, MaxConnWaitTimeout: duration, MaxConnsPerHost: 1000, MaxIdleConnDuration: 1000, MaxIdemponentCallAttempts: 1000, ReadTimeout: duration, WriteBufferSize: 1000, WriteTimeout: duration, MaxConnDuration: duration}
	return client
}
