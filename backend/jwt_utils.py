import jwt
from jwt import ExpiredSignatureError
from jwt.exceptions import PyJWTError
from datetime import datetime, timedelta
from fastapi import HTTPException
from pydantic import BaseModel
import os
from dotenv import load_dotenv
from fastapi import Header

load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM")


class TokenData(BaseModel):
    id : int
    username: str

def create_access_token(data: TokenData):
    to_encode = data.dict()

    expire = datetime.utcnow() + timedelta(hours=3)
    to_encode.update({"exp": expire})

    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def decode_token(token: str):
    try:
        decoded_token = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return decoded_token
    except ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except PyJWTError:
        raise HTTPException(status_code=401, detail="Could not validate credentials")


def get_current_username(token: str = Header(...)):
    decoded = decode_token(token)
    return decoded.get("username")

def get_current_username_from_header(Authorization: str = Header(...)):  # Use Authorization header if needed
    token = Authorization.split(" ")[1]
    decoded = decode_token(token)
    return decoded.get("username")


