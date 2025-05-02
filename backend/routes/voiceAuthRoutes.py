from fastapi import APIRouter, File, UploadFile, HTTPException, Form, Depends
from fastapi.responses import JSONResponse
from VoiceAuthentication.voiceAuth import verify_voice, speech_to_text
from fastapi.responses import JSONResponse
from VoiceAuthentication.audio_files_manager import TEMP_DIR, convert_to_wav, delete_temp_files
from jwt_utils import decode_token, get_current_role
import os
import time
from typing import Annotated
from pydantic import BaseModel
import joblib
from deviceManagementUtils import update_device_status
from database import get_session
from sqlalchemy.orm import Session

from models.userModel import RoleEnum
from serialCommunicationUtils import send_message


voiceAuthRouter = APIRouter()
VOICES_DIR = "voices/"
FAILED_ATTEMPTS_DIR = "failed_attempts/"
os.makedirs(VOICES_DIR, exist_ok=True)
os.makedirs(FAILED_ATTEMPTS_DIR, exist_ok=True)

pipeline = joblib.load("NLP/nlp_intent_pipeline.pkl")
PREDEFINED_ACTIONS = ["door_lock", "door_unlock", "heater_on", "heater_off"]


# Dictionary to track failed attempt with the time of failure
failed_attempts_tracker = {}

MAX_FAILED_ATTEMPTS = 3
MAX_ATTEMPT_TIME = 300 # 300 seconds which means 5 minutes

SessionDep = Annotated[Session, Depends(get_session)]


device_action_mapping = {
    "lights_on": ("lights", True),
    "lights_off": ("lights", False),
    "heater_on": ("heater", True),
    "heater_off": ("heater", False),
    "door_lock": ("door", False),
    "door_unlock": ("door", True)
}

@voiceAuthRouter.post("/voice-upload")
async def upload_voice(token: str = Form(...), audio: UploadFile = File(...)):
    """
    Receives an AAC audio file, converts it to WAV, resamples to 16kHz, and saves it in voices/{username}.wav.
    """
    if not token:
        raise HTTPException(status_code=400, detail="Token is required")

    # Save the raw AAC file temporarily
    username = decode_token(token)["username"]
    raw_aac_path = os.path.join(TEMP_DIR, f"{username}.aac")
    with open(raw_aac_path, "wb") as f:
        f.write(await audio.read())

    # Convert AAC to WAV
    wav_path = os.path.join(TEMP_DIR, f"{username}.wav")
    convert_to_wav(raw_aac_path, wav_path)

    # No need to resample 
    final_audio_path = os.path.join(VOICES_DIR, f"{username}.wav")
    os.rename(wav_path, final_audio_path)


    return JSONResponse(
        content={"message": "Audio processed successfully", "file_path": final_audio_path},
        status_code=200
    )

@voiceAuthRouter.post("/voice-authentication")
async def authenticate_voice(session: SessionDep, token: str = Form(...), audio: UploadFile = File(...)):
    """
    Authenticates a user based on voice by comparing the uploaded AAC file with the saved voice sample.
    """
    if not token:
        raise HTTPException(status_code=400, detail="Token is required")
    
    role = get_current_role(token)
    if role != RoleEnum.admin :
        raise HTTPException(status_code=403, detail="Guests are not allowed to use voice commands")
        

    # Decode token to get username
    username = decode_token(token)["username"]
    saved_voice_path = os.path.join(VOICES_DIR, f"{username}.wav")

    if not os.path.exists(saved_voice_path):
        raise HTTPException(status_code=404, detail="No saved voice sample found for this user")

    # Initialize failed attempts tracker for the user if not present
    if username not in failed_attempts_tracker:
        failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": time.time()}

    # Reset failed attempts if 5 minutes have passed since the last failed attempt
    if time.time() - failed_attempts_tracker[username]["last_failed_time"] > MAX_ATTEMPT_TIME:
        failed_attempts_tracker[username]["attempts"] = 0

    # Check if the user has reached the limit of failed attempts
    if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
        raise HTTPException(status_code=403, detail="Too many failed attempts, please try again after 5 minutes.")

    # Save raw audio
    raw_aac_path = os.path.join(TEMP_DIR, f"{username}_temp.aac")
    with open(raw_aac_path, "wb") as f:
        f.write(await audio.read())

    # Convert
    temp_wav_path = os.path.join(TEMP_DIR, f"{username}_temp.wav")
    convert_to_wav(raw_aac_path, temp_wav_path)

    # Verify voice
    is_match = verify_voice(saved_voice_path, temp_wav_path)

    # Handle authentication result
    if is_match:
        # Reset failed attempts on successful authentication
        failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": time.time()}
        transcription = speech_to_text(temp_wav_path)
        print(f"Transcription: {transcription}")

        # Pass the transcription to the AI model for prediction

        prediction = pipeline.predict([transcription]).tolist()[0]

        send_message(prediction)

        # Clean up temporary files
        delete_temp_files()
        return JSONResponse(content={"message": "Authentication successful"}, status_code=200)
    else:
        # Increment failed attempts
        failed_attempts_tracker[username]["attempts"] += 1
        failed_attempts_tracker[username]["last_failed_time"] = time.time()

        # Save the failed audio after 3 failed attempts
        if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
            # failed_audio_path = os.path.join(FAILED_ATTEMPTS_DIR, f"{username}_failed_{int(time.time())}.aac")
            # with open(failed_audio_path, "wb") as f:
            #     f.write(await audio.read())
            failed_wav_path = os.path.join(FAILED_ATTEMPTS_DIR, f"{username}_failed_{int(time.time())}.wav")
            convert_to_wav(raw_aac_path, failed_wav_path)

        # Clean up temporary files
        delete_temp_files()
        raise HTTPException(status_code=401, detail="The provided voice does not match the user's profile.")


@voiceAuthRouter.post("/voice-control")
async def authenticate_voice(token: str = Form(...), audio: UploadFile = File(...)):
    """
    Authenticates a user based on voice by comparing the uploaded AAC file with the saved voice sample.
    """
    if not token:
        raise HTTPException(status_code=400, detail="Token is required")

    # Decode token to get username
    username = decode_token(token)["username"]
    saved_voice_path = os.path.join(VOICES_DIR, f"{username}.wav")

    if not os.path.exists(saved_voice_path):
        raise HTTPException(status_code=404, detail="No saved voice sample found for this user")

    # Initialize failed attempts tracker for the user if not present
    if username not in failed_attempts_tracker:
        failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": time.time()}

    # Reset failed attempts if 5 minutes have passed since the last failed attempt
    if time.time() - failed_attempts_tracker[username]["last_failed_time"] > MAX_ATTEMPT_TIME:
        failed_attempts_tracker[username]["attempts"] = 0

    # Check if the user has reached the limit of failed attempts
    if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
        raise HTTPException(status_code=403, detail="Too many failed attempts, please try again after 5 minutes.")

    # Save raw audio
    raw_aac_path = os.path.join(TEMP_DIR, f"{username}_temp.aac")
    with open(raw_aac_path, "wb") as f:
        f.write(await audio.read())

    # Convert
    temp_wav_path = os.path.join(TEMP_DIR, f"{username}_temp.wav")
    convert_to_wav(raw_aac_path, temp_wav_path)

    # Speech to text
    transcription = speech_to_text(temp_wav_path)
    print(f"Transcription: {transcription}")

    # Process transcription with AI model (NLP model)
    command_vec = vectorizer.transform([transcription])
    prediction = model.predict(command_vec)[0]

    if prediction in PREDEFINED_ACTIONS:
        # Perform voice authentication
        is_match = verify_voice(saved_voice_path, temp_wav_path)

        if is_match:
            failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": time.time()}
            # Clean up temporary files
            delete_temp_files()
            return JSONResponse(content={"message": "Authentication successful", "isAuthenticated": True},
                                status_code=200)
        else:
            failed_attempts_tracker[username]["attempts"] += 1
            failed_attempts_tracker[username]["last_failed_time"] = time.time()

            # Save failed audio after 3 failed attempts
            if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
                failed_wav_path = os.path.join(FAILED_ATTEMPTS_DIR, f"{username}_failed_{int(time.time())}.wav")
                convert_to_wav(raw_aac_path, failed_wav_path)

            # Clean up temporary files
            delete_temp_files()
            return JSONResponse(content={"message": "Authentication failed", "isAuthenticated": False}, status_code=401)

    else:
        # If the action is not recognized, return success without voice authentication
        delete_temp_files()
        return JSONResponse(content={"message": "Command successful without voice authentication", "isAuthenticated": True}, status_code=200)


