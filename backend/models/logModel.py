from datetime import datetime
from sqlmodel import SQLModel, Field
from enum import Enum


class SourceEnum(str, Enum):
    manual = "manual"
    voice = "voice"
    timer = "timer"


class Log(SQLModel, table=True) :
    id: int = Field(default=None, primary_key=True)
    user : str = Field(index=True)
    command : str = Field(index=True)
    source : SourceEnum = Field(index=True, default=SourceEnum.manual)
    issued_at: datetime = Field(default_factory=datetime.utcnow)
