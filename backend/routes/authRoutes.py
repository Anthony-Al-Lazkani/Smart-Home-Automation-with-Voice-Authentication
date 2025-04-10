from fastapi import APIRouter, HTTPException, Depends, Query
from fastapi.responses import JSONResponse
from sqlmodel import Session, SQLModel, select
from sqlalchemy.orm import Session
from typing import Annotated
from models.userModel import User
from database import get_session
from pydantic import BaseModel
from passlib.context import CryptContext
from jwt_utils import TokenData, create_access_token, decode_token
import jwt

authRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


class signUpRequest(BaseModel):
    username: str
    email: str
    password: str
    confirm_password: str


class signInRequest(BaseModel):
    username: str
    password: str




@authRouter.post("/register")
async def sign_up(user: signUpRequest, session: SessionDep) -> JSONResponse:
    exisiting_username = session.exec(select(User).where(User.username == user.username)).first()

    if len(user.username) < 3:
        raise HTTPException(status_code=400, detail="Username should be at least 3 characters")

    if len(user.username) > 50:
        raise HTTPException(status_code=400, detail="Username should be less than 50 characters")

    if len(user.password) < 8:
        raise HTTPException(status_code=400, detail="Password should be at least 8 characters")

    if exisiting_username:
        raise HTTPException(status_code=400, detail="Username already Taken !")

    existing_email = session.exec(select(User).where(User.email == user.email)).first()
    if existing_email:
        raise HTTPException(status_code=400, detail="Email already registered !")

    if user.password != user.confirm_password:
        raise HTTPException(status_code=400, detail="Passwords do not match !")

    hashed_password = pwd_context.hash(user.password)
    new_user = User(username=user.username, email=user.email, password=hashed_password)

    session.add(new_user)
    session.commit()
    session.refresh(new_user)

    token_data = TokenData(id=new_user.id, username=user.username)
    token = create_access_token(token_data)

    return JSONResponse(
        content={"message": "User created successfully", "token" : token},
        status_code=201
    )


@authRouter.post("/login")
async def login(user: signInRequest, session: SessionDep) -> JSONResponse:
    existing_username = session.exec(select(User).where(User.username == user.username)).first()
    if not existing_username:
        raise HTTPException(status_code=404, detail="Username not found !")

    if not pwd_context.verify(user.password, existing_username.password):
        raise HTTPException(status_code=400, detail="Invalid Password !")

    token_data = TokenData(id=existing_username.id, username=user.username)
    token = create_access_token(token_data)

    return JSONResponse(
        content={"message": "Login successful","token" : token},
        status_code=200
    )


@authRouter.get("/users")
def read_users(
        session: SessionDep,
        offset: int = 0,
        limit: Annotated[int, Query(le=100)] = 100,
) -> list[User]:
    users = session.exec(select(User).offset(offset).limit(limit)).all()
    return users



@authRouter.delete("/users")
async def delete_all_users(session: SessionDep) -> JSONResponse:
    try:
        deleted_users = session.exec(select(User)).all()
        if not deleted_users:
            raise HTTPException(status_code=404, detail="No users found to delete")

        for user in deleted_users:
            session.delete(user)

        session.commit()

        return JSONResponse(content={"message": "All users deleted successfully"}, status_code=200)

    except Exception as e:
        session.rollback()
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")




# class SetPinRequest(BaseModel):
#     token: str
#     pin_code: str
#
# @authRouter.post("/pin-setup")
# async def set_pin(request: SetPinRequest, session: Session = Depends(get_session)):
#     # Decode the token to get the user ID
#     decoded_token = decode_token(request.token)
#     user_id = decoded_token.get("id")
#
#     if not user_id:
#         raise HTTPException(status_code=400, detail="Invalid token")
#
#     # Fetch the user from the database
#     existing_user = session.query(User).filter(User.id == user_id).first()
#
#     if not existing_user:
#         raise HTTPException(status_code=404, detail="User not found")
#
#     if existing_user.pin_code:
#         raise HTTPException(status_code=400, detail="A PIN has already been set for this account")
#
#     # Hash the PIN and save it
#     hashed_pin = pwd_context.hash(request.pin_code)
#     existing_user.pin_code = hashed_pin
#
#     session.commit()
#
#     return JSONResponse(content={"message": "PIN code set successfully"}, status_code=201)
#
#
# class PinAuthRequest(BaseModel):
#     token: str
#     pin_code: str
#
# @authRouter.post("/pin-authentication")
# async def authenticate_pin(request: PinAuthRequest, session: Session = Depends(get_session)):
#     # Decode the token to get the user ID
#     decoded_token = decode_token(request.token)
#     user_id = decoded_token.get("id")
#
#     if not user_id:
#         raise HTTPException(status_code=400, detail="Invalid token")
#
#     # Fetch the user from the database
#     existing_user = session.query(User).filter(User.id == user_id).first()
#
#     if not existing_user:
#         raise HTTPException(status_code=404, detail="User not found")
#
#     if not existing_user.pin_code:
#         raise HTTPException(status_code=400, detail="No PIN has been set for this account")
#
#     # Verify the provided PIN with the stored hashed PIN
#     if not pwd_context.verify(request.pin_code, existing_user.pin_code):
#         raise HTTPException(status_code=400, detail="Invalid PIN")
#
#     return JSONResponse(content={"message": "PIN code authenticated successfully"}, status_code=200)



