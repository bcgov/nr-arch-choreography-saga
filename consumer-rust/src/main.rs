use actix_web::{get, App, HttpServer};
use cloudevents::{Event, EventBuilder, EventBuilderV10};
use serde_json::json;
use std::{env};
use async_nats::jetstream::consumer::PushConsumer;
use futures::{StreamExt};
#[get("/")]
async fn get_event() -> Event {
  let payload = json!({"hello": "world"});

  EventBuilderV10::new()
    .id("0001")
    .ty("example.test")
    .source("http://localhost/")
    .data("application/json", payload)
    .extension("someint", "10")
    .build()
    .unwrap()
}
async fn handle_nats_message() {
  let nats_url = env::var("NATS_URL").unwrap_or_else(|_| "nats://localhost:4222".to_string());
  println!("NATS_URL: {}", nats_url);

  let client = async_nats::connect(nats_url).await.unwrap();
  let mut subscriber = client.subscribe("EVENTS.TOPIC".into()).await.unwrap();
  while let Some(message) = subscriber.next().await {
    println!("Received message {:?}", message);
  }
}
async fn handle_jet_stream_message() {
  let nats_url = env::var("NATS_URL").unwrap_or_else(|_| "nats://localhost:4222".to_string());
  println!("NATS_URL: {}", nats_url);

  let client = async_nats::connect(nats_url).await.unwrap();
  let context = async_nats::jetstream::new(client.clone());
  context.get_or_create_stream(async_nats::jetstream::stream::Config {
    name: "EVENTS".to_string(),
    subjects: vec!["EVENTS-TOPIC".to_string()],
    ..Default::default()
  }).await.expect("TODO: panic message");
  let stream = context.get_stream("EVENTS").await.unwrap();
  stream
    .create_consumer(async_nats::jetstream::consumer::push::Config {
      deliver_subject: "consumer-rust".to_string(),
      durable_name: Some("consumer-rust".to_string()),
      ack_policy: async_nats::jetstream::consumer::AckPolicy::Explicit,
      ack_wait: std::time::Duration::from_secs(30),
      deliver_policy: async_nats::jetstream::consumer::DeliverPolicy::New,
      ..Default::default()
    })
    .await
    .unwrap();
  let consumer: PushConsumer = stream.get_consumer("consumer-rust").await.unwrap();
  let mut messages = consumer.messages().await.unwrap();
  while let Some(Ok(message)) = messages.next().await {
    message.ack().await.unwrap();
    println!("Received message status[{:?}], payload[{:?}]", message.status, message.payload);
  }
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
  env::set_var("RUST_LOG", "actix_server=info,actix_web=info");
  env_logger::init();
  tokio::spawn(async {
    handle_nats_message().await;
  });
  tokio::spawn(async {
    handle_jet_stream_message().await;
  });
  let port: u16 = env::var("PORT").unwrap_or_else(|_| 3000.to_string()).parse().unwrap();
  HttpServer::new(|| {
    App::new()
      .wrap(actix_cors::Cors::permissive())
      .wrap(actix_web::middleware::Logger::default().exclude("/").exclude("/favicon.ico"))
      .service(get_event)
  })
    .bind(("0.0.0.0", port))?
    .workers(1)
    .run()
    .await
}
