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
	"log"
	"os"
)

var (
	buf    bytes.Buffer
	sysLog = log.New(&buf, "log: ", log.Llongfile)
)

func main() {
	_ = godotenv.Load()
	app := fiber.New(fiber.Config{})
	app.Use(helmet.New())
	app.Use(favicon.New())
	app.Use(recover.New())
	app.Use(cors.New())
	app.Use(csrf.New())

	app.Use(logger.New(logger.Config{
		TimeFormat: "2006-01-02T15:04:05",
		TimeZone:   "GMT",
	}))
	app.Get("/", func(c *fiber.Ctx) error {
		return c.SendString("Hello, World!")
	})
	nc, err := nats.Connect(getEnv("NATS_URL", "nats://localhost:4222"))
	if err != nil {
		sysLog.Fatalf("Error: %v", err)
		os.Exit(127)
	}
	js, _ := nc.JetStream()
	_, subErr := js.QueueSubscribe("EVENTS-TOPIC", "CONSUMER-GO", handler, nats.Durable("CONSUMER-GO"))

	if subErr != nil {
		sysLog.Fatalf("Error: %v", subErr)
		os.Exit(127)
	}
	HandleSubscriptionForExternalAPIAndNotifyUsingHttp(nc)
	//convert to number from string
	PORT := getEnv("PORT", "3000")
	var port = fmt.Sprintf(":%s", PORT)
	appErr := app.Listen(port)
	if appErr != nil {
		sysLog.Fatalf("Error: %v", appErr)
		os.Exit(127)
	}

}

func handler(msg *nats.Msg) {
	var meta, _ = msg.Metadata()

	var message = fmt.Sprintf("Received a message: seq[%d], pending[%d], data[%s]", meta.Sequence, meta.NumPending, string(msg.Data))

	fmt.Println(message)
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}
