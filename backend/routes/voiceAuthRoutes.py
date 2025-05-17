import asyncio

import torchaudio
from dotenv import load_dotenv
from fastapi import APIRouter, File, UploadFile, HTTPException, Form, Depends
from fastapi.responses import JSONResponse
from sqlmodel import select

from VoiceAuthentication.voiceAuth import verify_voice, speech_to_text, verify_voice_enhanced, speech_to_text_enhanced
from fastapi.responses import JSONResponse
from VoiceAuthentication.audio_files_manager import TEMP_DIR, convert_to_wav, delete_temp_files
from cryptoUtils import encrypt_file, decrypt_file
from jwt_utils import decode_token, get_current_username, TokenData, create_access_token
import os
import time
from typing import Annotated
from pydantic import BaseModel
import joblib
from deviceManagementUtils import update_device_status, create_log
from database import get_session
from sqlalchemy.orm import Session
from models.logModel import SourceEnum
from models.userModel import RoleEnum, User
from serialCommunicationUtils import send_message


voiceAuthRouter = APIRouter()
VOICES_DIR = "voices/"
VOICES_DIR_ENC = "voices_ENC/"
FAILED_ATTEMPTS_DIR = "failed_attempts/"
os.makedirs(VOICES_DIR, exist_ok=True)
os.makedirs(VOICES_DIR_ENC, exist_ok=True)
os.makedirs(FAILED_ATTEMPTS_DIR, exist_ok=True)

pipeline = joblib.load("NLP/nlp_intent_pipeline.pkl")
PREDEFINED_ACTIONS = ["door_lock", "door_unlock", "heater_on", "heater_off"]


# Dictionary to track failed attempt with the time of failure
failed_attempts_tracker = {}

MAX_FAILED_ATTEMPTS = 3
MAX_ATTEMPT_TIME = 300 # 300 seconds which means 5 minutes

SessionDep = Annotated[Session, Depends(get_session)]

load_dotenv()


# @voiceAuthRouter.post("/voice-upload")
# async def upload_voice(token: str = Form(...), audio: UploadFile = File(...)):
#     """
#     Receives an AAC audio file, converts it to WAV, resamples to 16kHz, and saves it in voices/{username}.wav.
#     """
#     if not token:
#         raise HTTPException(status_code=400, detail="Token is required")
#
#     # Save the raw AAC file temporarily
#     username = decode_token(token)["username"]
#     raw_aac_path = os.path.join(TEMP_DIR, f"{username}.aac")
#     with open(raw_aac_path, "wb") as f:
#         f.write(await audio.read())
#
#     # Convert AAC to WAV
#     wav_path = os.path.join(TEMP_DIR, f"{username}.wav")
#     convert_to_wav(raw_aac_path, wav_path)
#
#     # No need to resample
#     final_audio_path = os.path.join(VOICES_DIR, f"{username}.wav")
#     os.rename(wav_path, final_audio_path)
#
#
#     return JSONResponse(
#         content={"message": "Audio processed successfully", "file_path": final_audio_path},
#         status_code=200
#     )

@voiceAuthRouter.post("/voice-upload")
async def upload_voice(session: SessionDep, token: str = Form(...), audio: UploadFile = File(...)):
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
    final_audio_path = os.path.join(VOICES_DIR_ENC, f"{username}.wav.enc")
    encrypt_file(wav_path, final_audio_path)
    os.remove(wav_path)

    user = session.exec(select(User).where(User.username == username)).first()

    new_token_data = TokenData(id=user.id, username=user.username, isVoiceUploaded=True)
    new_token = create_access_token(new_token_data)


    return JSONResponse(
        content={"message": "Audio processed successfully", "token" : new_token},
        status_code=201
    )

# @voiceAuthRouter.post("/voice-authentication")
# async def authenticate_voice(session: SessionDep, token: str = Form(...), audio: UploadFile = File(...)):
#     start_time = time.time()
#     if not token:
#         raise HTTPException(status_code=400, detail="Token is required")
#
#     current_username = get_current_username(token)
#     current_user = session.exec(select(User).where(User.username == current_username)).first()
#
#     if current_user.role in [RoleEnum.guest, RoleEnum.user]:
#         raise HTTPException(status_code=403, detail="This role is not allowed to use voice commands")
#
#     username = decode_token(token)["username"]
#     saved_voice_path = os.path.join(VOICES_DIR, f"{username}.wav")
#
#     if not os.path.exists(saved_voice_path):
#         raise HTTPException(status_code=404, detail="No saved voice sample found for this user")
#
#     # Failed attempts logic
#     now = time.time()
#     if username not in failed_attempts_tracker:
#         failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": now}
#     if now - failed_attempts_tracker[username]["last_failed_time"] > MAX_ATTEMPT_TIME:
#         failed_attempts_tracker[username]["attempts"] = 0
#     if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
#         raise HTTPException(status_code=403, detail="Too many failed attempts, try again later.")
#
#     # Save uploaded audio
#     raw_aac_path = os.path.join(TEMP_DIR, f"{username}_temp.aac")
#     with open(raw_aac_path, "wb") as f:
#         f.write(await audio.read())
#
#     temp_wav_path = os.path.join(TEMP_DIR, f"{username}_temp.wav")
#
#
#     convert_to_wav(raw_aac_path, temp_wav_path)
#
#     # Load audio once
#     signal2, fs2 = torchaudio.load(temp_wav_path)
#
#     # Parallel: verify + transcribe
#     loop = asyncio.get_event_loop()
#
#     verify_task = loop.run_in_executor(None, verify_voice_enhanced, saved_voice_path, signal2)
#     transcribe_task = loop.run_in_executor(None, speech_to_text_enhanced, signal2)
#
#     is_match, transcription = await asyncio.gather(verify_task, transcribe_task)
#
#     print(f"Transcription : {transcription}")
#     if is_match:
#         failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": now}
#
#         prediction = pipeline.predict([transcription]).tolist()[0]
#         send_message(prediction)
#         await create_log(
#             user=username,
#             command=prediction,
#             source=SourceEnum.voice,
#             session=session
#         )
#         delete_temp_files()
#         end_time = time.time()
#         duration = end_time - start_time
#         print(f"Voice Authentication tasks took {duration:.2f} seconds")
#         return JSONResponse(content={"message": "Authentication successful"}, status_code=200)
#
#     else:
#         failed_attempts_tracker[username]["attempts"] += 1
#         failed_attempts_tracker[username]["last_failed_time"] = now
#
#         if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
#             failed_wav_path = os.path.join(FAILED_ATTEMPTS_DIR, f"{username}failed{int(now)}.wav")
#             convert_to_wav(raw_aac_path, failed_wav_path)
#
#         delete_temp_files()
#         raise HTTPException(status_code=401, detail="Voice does not match profile.")

@voiceAuthRouter.post("/voice-authentication")
async def authenticate_voice(
    session: SessionDep,
    token: str = Form(...),
    audio: UploadFile = File(...)
):
    """
    Authenticates user by comparing their voice to a previously uploaded sample.
    Also transcribes the voice and triggers a prediction pipeline if matched.
    """
    start_time = time.time()

    if not token:
        raise HTTPException(status_code=400, detail="Token is required")

    # Get current username
    try:
        username = decode_token(token)["username"]
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token")

    # Validate user role
    current_user = session.exec(select(User).where(User.username == username)).first()
    if current_user is None:
        raise HTTPException(status_code=404, detail="User not found")

    if current_user.role in [RoleEnum.guest, RoleEnum.user]:
        raise HTTPException(status_code=403, detail="This role is not allowed to use voice commands")

    # Check if encrypted voice sample exists
    encrypted_voice_path = os.path.join(VOICES_DIR_ENC, f"{username}.wav.enc")
    if not os.path.exists(encrypted_voice_path):
        raise HTTPException(status_code=404, detail="No saved voice sample found for this user")

    # Decrypt for comparison
    decrypted_path = os.path.join(TEMP_DIR, f"{username}_ref.wav")
    decrypt_file(encrypted_voice_path, decrypted_path)

    # Check failed attempts
    now = time.time()
    if username not in failed_attempts_tracker:
        failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": now}
    if now - failed_attempts_tracker[username]["last_failed_time"] > MAX_ATTEMPT_TIME:
        failed_attempts_tracker[username]["attempts"] = 0
    if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
        raise HTTPException(status_code=403, detail="Too many failed attempts, try again later.")

    # Save uploaded audio (AAC) and convert to WAV
    raw_aac_path = os.path.join(TEMP_DIR, f"{username}_temp.aac")
    with open(raw_aac_path, "wb") as f:
        f.write(await audio.read())

    temp_wav_path = os.path.join(TEMP_DIR, f"{username}_temp.wav")
    convert_to_wav(raw_aac_path, temp_wav_path)

    # Load uploaded WAV
    signal2, fs2 = torchaudio.load(temp_wav_path)

    # Run voice verification and transcription in parallel
    loop = asyncio.get_event_loop()
    verify_task = loop.run_in_executor(None, verify_voice_enhanced, decrypted_path, signal2)
    transcribe_task = loop.run_in_executor(None, speech_to_text_enhanced, signal2)
    is_match, transcription = await asyncio.gather(verify_task, transcribe_task)

    print(f"Transcription: {transcription}")

    if is_match:
        failed_attempts_tracker[username] = {"attempts": 0, "last_failed_time": now}

        prediction = pipeline.predict([transcription]).tolist()[0]
        send_message(prediction)

        await create_log(
            user=username,
            command=prediction,
            source=SourceEnum.voice,
            session=session
        )

        delete_temp_files()
        print(f"Voice authentication took: {time.time() - start_time:.2f}s")
        return JSONResponse(content={"message": "Authentication successful"}, status_code=200)

    else:
        failed_attempts_tracker[username]["attempts"] += 1
        failed_attempts_tracker[username]["last_failed_time"] = now

        # Save failed attempt if limit is hit
        if failed_attempts_tracker[username]["attempts"] >= MAX_FAILED_ATTEMPTS:
            failed_path = os.path.join(FAILED_ATTEMPTS_DIR, f"{username}_failed_{int(now)}.wav")
            convert_to_wav(raw_aac_path, failed_path)

        delete_temp_files()
        raise HTTPException(status_code=401, detail="Voice does not match profile.")