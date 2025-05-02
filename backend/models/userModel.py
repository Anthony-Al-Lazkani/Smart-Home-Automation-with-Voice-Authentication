from sqlmodel import Field, SQLModel
from enum import Enum

class RoleEnum(str, Enum):
    admin = "admin"
    family = "family"
    user = "user"
    guest = "guest"

class User(SQLModel, table=True):
    id: int= Field(default=None, primary_key=True)
    username: str = Field(index=True)
    email: str = Field(default=None, index=True, unique=True)
    password : str
    role : RoleEnum = Field(default=RoleEnum.guest)
