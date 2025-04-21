from sqlmodel import Field, SQLModel
from typing import Optional


class DeviceTimer(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    device_type: str = Field(index=True, unique=True)
    on_time: Optional[str] = Field(default=None)  # Format: HH:mm
    off_time: Optional[str] = Field(default=None)  # Format: HH:mm
