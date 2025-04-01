from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

#Routes
from routes.authRoutes import authRouter
from routes.nlpRoutes import nlpRouter
from routes.voiceAuthRoutes import voiceAuthRouter
from database import create_db_and_tables

app = FastAPI()


@app.on_event("startup")
def on_startup():
    create_db_and_tables()


app.include_router(authRouter, prefix="/auth")
app.include_router(nlpRouter, prefix="/nlp")
app.include_router(voiceAuthRouter, prefix="/voice-auth")


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
