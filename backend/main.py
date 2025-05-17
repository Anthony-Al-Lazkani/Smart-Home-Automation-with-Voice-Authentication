import asyncio

import joblib
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

#Routes
from routes.authRoutes import authRouter
from routes.voiceAuthRoutes import voiceAuthRouter
from routes.accountManagementRoutes import accountManagementRouter
from routes.deviceManagementRoutes import deviceManagementRouter
from routes.logRoutes import logRouter
from routes.timerRoutes import timerRouter
from database import create_db_and_tables


# utils
from serialCommunicationUtils import open_serial_connection, close_serial_connection, start_listener_thread, \
    stop_listener

app = FastAPI()

isConnected = True


@app.on_event("startup")
async def on_startup():
    create_db_and_tables()
    if isConnected:
        start_listener_thread()

@app.on_event("shutdown")
def shutdown_event():
    if isConnected:
        stop_listener()

app.include_router(authRouter, prefix="/auth")
app.include_router(voiceAuthRouter, prefix="/voice-auth")
app.include_router(accountManagementRouter, prefix="/account")
app.include_router(timerRouter, prefix="/timer")
app.include_router(logRouter, prefix="/logs")
app.include_router(deviceManagementRouter)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
