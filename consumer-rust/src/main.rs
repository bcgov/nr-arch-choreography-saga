use actix_web::{get, App, HttpServer};
use cloudevents::{Event, EventBuilder, EventBuilderV10};
use serde_json::json;
use std::{env};

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

#[actix_web::main]
async fn main() -> std::io::Result<()> {
  env::set_var("RUST_LOG", "actix_server=info,actix_web=info");
  env_logger::init();
  println!("starting HTTP server at http://localhost:3000");
  let nats_url = env::var("NATS_URL").unwrap_or_else(|_| "nats://localhost:4222".to_string());
  println!("NATS_URL: {}", nats_url);


  HttpServer::new(|| {
    App::new()
      .wrap(actix_cors::Cors::permissive())
      .wrap(actix_web::middleware::Logger::default())
      .service(get_event)
  })
    .bind(("0.0.0.0", 3000))?
    .workers(1)
    .run()
    .await
}
