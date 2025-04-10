import joblib


pipeline = joblib.load("nlp_intent_pipeline.pkl")


def predict(transcription : str):
    prediction = pipeline.predict([transcription]).tolist()[0]
    print("prediction : ", prediction)

command = "can you please deactivate the heater"

predict(command)



