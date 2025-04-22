from fastapi import APIRouter, HTTPException, Depends, Query, Body
from fastapi.responses import JSONResponse
from sqlmodel import Session, SQLModel, select
from sqlalchemy.orm import Session
from typing import Annotated
from models.userModel import User
from database import get_session
from pydantic import BaseModel
from passlib.context import CryptContext
from jwt_utils import TokenData, create_access_token, decode_token, get_current_role
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

    token_data = TokenData(id=new_user.id, username=user.username, role="admin")
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

    token_data = TokenData(id=existing_username.id, username=user.username, role="admin")
    token = create_access_token(token_data)

    return JSONResponse(
        content={"message": "Login successful","token" : token},
        status_code=200
    )

@authRouter.post("/login/guest")
async def guest_login() -> JSONResponse:
    token_data = TokenData(id=999, username="guest", role="guest")
    token = create_access_token(token_data)

    return JSONResponse(
        content={"message": "Guest login successful", "token": token},
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


