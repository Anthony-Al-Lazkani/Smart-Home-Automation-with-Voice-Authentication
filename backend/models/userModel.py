from sqlmodel import Field, SQLModel
from typing import Optional

class User(SQLModel, table=True):
    id: int= Field(default=None, primary_key=True)
    username: str = Field(index=True)
    email: str = Field(default=None, index=True, unique=True)
    password : str
    pin_code : Optional[str]
