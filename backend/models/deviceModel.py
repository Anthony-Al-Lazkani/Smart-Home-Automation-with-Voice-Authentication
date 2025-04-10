from sqlmodel import SQLModel, Field
from datetime import datetime, timezone


class Device(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    device_name: str = Field(index=True, unique=True)
    status: bool = Field(default=False)  # False = off, True = on
    last_updated: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))