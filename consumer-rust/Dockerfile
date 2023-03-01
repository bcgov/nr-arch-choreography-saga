FROM rust:1.67 AS build
WORKDIR /usr/src
RUN rustup target add x86_64-unknown-linux-musl

# Download and compile Rust dependencies (and store as a separate Docker layer)
RUN USER=root cargo new app
WORKDIR /usr/src/app
COPY Cargo.toml Cargo.lock ./
RUN cargo install --target x86_64-unknown-linux-musl --path .

# Build our application
COPY src ./src
RUN cargo build --release --target x86_64-unknown-linux-musl

# Copy into a scratch (no-op) container, only copying the binary
FROM scratch
COPY --from=build /usr/src/app/target/x86_64-unknown-linux-musl/release/app .
EXPOSE 3000
USER 1000
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/:3000
CMD ["./app"]
