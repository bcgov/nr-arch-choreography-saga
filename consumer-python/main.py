import asyncio
import nest_asyncio
from fastapi import FastAPI

import messaging.messagehandler as messagehandler

app = FastAPI(title="Consumer Python", version="0.0.1")
jsMsgHandler = messagehandler.MessageHandler("EVENTS-TOPIC", "consumer-python", "consumer-python")


async def connect():
    await jsMsgHandler.connect()


nest_asyncio.apply()
asyncio.run(connect())


@app.get("/")
async def root():
    return {"message": "Hello World"}


@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}
