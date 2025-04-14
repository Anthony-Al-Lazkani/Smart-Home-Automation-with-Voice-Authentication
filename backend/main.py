from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

#Routes
from routes.authRoutes import authRouter
from routes.nlpRoutes import nlpRouter
from routes.voiceAuthRoutes import voiceAuthRouter
from routes.accountManagementRoutes import accountManagementRouter
from routes.deviceManagementRoutes import deviceManagementRouter
from database import create_db_and_tables

# utils
from serialCommunicationUtils import open_serial_connection, close_serial_connection

app = FastAPI()

isConnected = False


@app.on_event("startup")
def on_startup():
    create_db_and_tables()
    if isConnected:
        open_serial_connection()

@app.on_event("shutdown")
def shutdown_event():
    if isConnected:
        close_serial_connection()


app.include_router(authRouter, prefix="/auth")
app.include_router(nlpRouter, prefix="/nlp")
app.include_router(voiceAuthRouter, prefix="/voice-auth")
app.include_router(accountManagementRouter, prefix="/account")
app.include_router(deviceManagementRouter)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
