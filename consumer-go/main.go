package main

import (
	"bytes"
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
	err := app.Listen(":3000")
	if err != nil {
		sysLog.Fatalf("Error: %v", err)
		return
	}
	nc, err := nats.Connect(os.Getenv("NATS_URL"))
	js, _ := nc.JetStream()
	js.QueueSubscribe("EVENTS", "CONSUMER-GO", handler, nats.Durable("CONSUMER-GO"))
}

func handler(msg *nats.Msg) {
	sysLog.Printf("Received a message: %s	", string(msg.Data))
}
