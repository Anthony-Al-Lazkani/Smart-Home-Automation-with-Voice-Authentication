from sqlmodel import SQLModel, Field
from typing import Optional
from datetime import datetime, timezone


class Indicator(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    indicator_name: str
    status: bool = Field(default=False)
    last_updated: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
