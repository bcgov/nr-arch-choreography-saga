FROM rust:1.67-alpine AS build
WORKDIR /usr/src
RUN apk add --no-cache musl-dev

# Download and compile Rust dependencies (and store as a separate Docker layer)
RUN USER=root cargo new app
WORKDIR /usr/src/app
COPY Cargo.toml Cargo.lock ./
RUN cargo install --path .

# Build our application
COPY src ./src
RUN cargo build --release

# Copy into a scratch (no-op) container, only copying the binary
FROM alpine:3.9.6
RUN apk add --no-cache libgcc
COPY --from=build /usr/src/app/target/release/consumer_rust /usr/local/bin/consumer_rust
EXPOSE 3000
USER 1000
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/:3000
CMD ["consumer_rust"]
