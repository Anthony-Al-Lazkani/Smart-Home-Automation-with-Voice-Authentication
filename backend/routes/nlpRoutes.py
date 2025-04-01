from fastapi import APIRouter, HTTPException
import joblib
from pydantic import BaseModel
import httpx


nlpRouter = APIRouter()

model = joblib.load("NLP/nlp_command_model.pkl")
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

        # url = f"http://192.168.1.39/{prediction}"
        # async with httpx.AsyncClient() as client:
        #     response = await client.get(url)
        print(command_txt, prediction)

        return {"action": prediction}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))