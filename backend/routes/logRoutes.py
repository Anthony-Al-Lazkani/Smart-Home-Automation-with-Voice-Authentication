from datetime import datetime
from sqlmodel import select
from typing import Annotated, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from starlette.responses import JSONResponse

from database import get_session
from pydantic import BaseModel

from deviceManagementUtils import create_log
from jwt_utils import oauth2_scheme, get_current_username
from models.logModel import SourceEnum, Log
from models.userModel import RoleEnum, User

logRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

class LogResponse(BaseModel):
    user : str
    command : str
    source : SourceEnum
    issued_at : datetime

@logRouter.get("/", response_model=List[LogResponse])
async def get_logs(session : SessionDep, token : str = Depends(oauth2_scheme)):
    current_username = get_current_username(token)

    existing_user = session.exec(select(User).where(User.username == current_username)).first()

    if not existing_user:
        raise HTTPException(
            status_code=404,
            detail= "User not found"
        )

    if existing_user.role != RoleEnum.admin :
        raise HTTPException(
            status_code=401,
            detail="Only admins can see past Logs"
        )

    logs = session.exec(select(Log)).all()

    return logs


# Route for testing purposes
class AddLogRequest(BaseModel):
    user : str
    command : str
    source : SourceEnum


@logRouter.post("/")
async def add_log(session : SessionDep, request : AddLogRequest):
    await create_log(
        user=request.user,
        command=request.command,
        source=request.source,
        session=session
    )

    return JSONResponse(
        status_code=200,
        content={"message" : "Log Added Successfully"}
    )
