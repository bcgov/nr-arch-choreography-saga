FROM node:lts-alpine AS builder

WORKDIR /app
COPY . ./
RUN npm ci --omit=dev

# Deployment container
FROM node:lts-alpine

# Set node to production
ENV NODE_ENV production

# Copy over app
WORKDIR /app
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/src ./src

# Expose port - mostly a convention, for readability
EXPOSE 3000

# Non-privileged user
RUN adduser -D app
USER app

# Start up command
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/:3000
ENTRYPOINT ["node", "src/server"]
