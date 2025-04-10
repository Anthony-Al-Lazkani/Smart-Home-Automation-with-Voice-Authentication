from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
import joblib
from pydantic import BaseModel


nlpRouter = APIRouter()

model = joblib.load("NLP/nlp_command_model.pkl")
pipeline = joblib.load("NLP/nlp_intent_pipeline.pkl")
vectorizer = joblib.load("NLP/nlp_vectorizer.pkl")

class NLPRequest(BaseModel):
    command : str

@nlpRouter.post("/command")
async def process_command(command : NLPRequest):
    try:
        command_txt = command.command
        # Transform the command
        command_vec = vectorizer.transform([command_txt])

        # Predict the intent
        prediction = model.predict(command_vec)[0]

        return JSONResponse(status_code=200, content={"prediction" : prediction})

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@nlpRouter.post("/command2")
async def process_command2(command : NLPRequest):
    prediction = pipeline.predict([command.command])
    prediction_list = prediction.tolist()

    return JSONResponse(status_code=200, content={"prediction with logistic" : prediction_list[0]})