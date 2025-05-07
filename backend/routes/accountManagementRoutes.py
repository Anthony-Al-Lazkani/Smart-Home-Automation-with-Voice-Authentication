from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import JSONResponse
from database import get_session
from sqlalchemy.orm import Session
from typing import Annotated
from passlib.context import CryptContext
from pydantic import BaseModel
from jwt_utils import decode_token
from sqlmodel import select
from models.userModel import User

accountManagementRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


class PasswordResetRequest(BaseModel):
    token: str
    old_password: str
    new_password: str


@accountManagementRouter.patch("/reset-password")
async def reset_password(reset_request: PasswordResetRequest, session: SessionDep) -> JSONResponse:
    # Decode the token and get user data
    decoded_token = decode_token(reset_request.token)
    user_id = decoded_token.get("id")
    username = decoded_token.get("username")


    if not user_id or not username:
        raise HTTPException(status_code=400, detail="Invalid token")

    # Fetch the user from the database using the user_id
    existing_user = session.exec(select(User).where(User.id == user_id)).first()

    if not existing_user:
        raise HTTPException(status_code=404, detail="User not found")

    # Verify the old password with the stored hashed password
    if not pwd_context.verify(reset_request.old_password, existing_user.password):
        raise HTTPException(status_code=400, detail="Old password is incorrect")

    # Check if the new password is different from the old password
    if reset_request.old_password == reset_request.new_password:
        raise HTTPException(status_code=400, detail="New password must be different from the old password")

    # Validate new password length
    if len(reset_request.new_password) < 8:
        raise HTTPException(status_code=400, detail="New password should be at least 8 characters")

    # Hash the new password and update it in the database
    hashed_new_password = pwd_context.hash(reset_request.new_password)
    existing_user.password = hashed_new_password

    # Commit the changes to the database
    session.commit()
    session.refresh(existing_user)

    return JSONResponse(
        content={"message": "Password updated successfully"},
        status_code=200
    )