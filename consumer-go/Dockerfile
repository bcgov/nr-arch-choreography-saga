FROM golang:1.20 AS build
WORKDIR /workspace/app
# pre-copy/cache go.mod for pre-downloading dependencies and only redownloading them in subsequent builds if they change
COPY go.mod go.sum ./
RUN go mod download && go mod verify
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -v -o /usr/local/bin/app ./...


FROM alpine:3.11.3
ARG DEPENDENCY=/usr/local/bin
RUN apk add --no-cache bash
RUN addgroup -S appgroup && adduser -S 1001 -G appgroup
USER 1001
COPY --chown=1001 --from=build ${DEPENDENCY}/app /usr/local/bin/app
EXPOSE 3000
ENTRYPOINT  ["app"]
