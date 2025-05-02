from fastapi import APIRouter, HTTPException, Depends, Query, Body, Header
from fastapi.responses import JSONResponse
from sqlmodel import Session, SQLModel, select
from sqlalchemy.orm import Session
from typing import Annotated
from models.userModel import User
from database import get_session
from pydantic import BaseModel
from passlib.context import CryptContext
from jwt_utils import TokenData, create_access_token, decode_token, get_current_role, get_current_username_from_header, \
    get_current_username, get_current_role_from_header
import jwt
from models.userModel import RoleEnum
from typing import List



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

    existing_users = session.exec(select(User)).all()
    if not existing_users:
        assigned_role = RoleEnum.admin
    else:
        assigned_role = RoleEnum.guest

    hashed_password = pwd_context.hash(user.password)
    new_user = User(username=user.username,
                    email=user.email,
                    password=hashed_password,
                    role=assigned_role
    )

    session.add(new_user)
    session.commit()
    session.refresh(new_user)

    token_data = TokenData(id=new_user.id, username=new_user.username, role=new_user.role)
    token = create_access_token(token_data)

    return JSONResponse(
        content={"message": "User created successfully", "token" : token},
        status_code=201
    )


@authRouter.post("/login")
async def login(user: signInRequest, session: SessionDep) -> JSONResponse:
    existing_user = session.exec(select(User).where(User.username == user.username)).first()
    if not existing_user:
        raise HTTPException(status_code=404, detail="Username not found !")

    if not pwd_context.verify(user.password, existing_user.password):
        raise HTTPException(status_code=400, detail="Invalid Password !")

    token_data = TokenData(id=existing_user.id, username=existing_user.username, role=existing_user.role)
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



# @authRouter.delete("/users")
# async def delete_all_users(session: SessionDep) -> JSONResponse:
#     try:
#         deleted_users = session.exec(select(User)).all()
#         if not deleted_users:
#             raise HTTPException(status_code=404, detail="No users found to delete")
#
#         for user in deleted_users:
#             session.delete(user)
#
#         session.commit()
#
#         return JSONResponse(content={"message": "All users deleted successfully"}, status_code=200)
#
#     except Exception as e:
#         session.rollback()
#         raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

class ChangeRoleRequest(BaseModel):
    token : str
    role: RoleEnum



@authRouter.put("/users/{username}/role")
async def update_user_role(username: str, request: ChangeRoleRequest,
                           session: SessionDep):

    current_user_role = get_current_role(request.token)
    current_username = get_current_username(request.token)

    if current_user_role != RoleEnum.admin:
        raise HTTPException(status_code=403, detail="Only admins can change user roles.")

    user_to_update = session.query(User).filter(User.username == username).first()

    if not user_to_update:
        raise HTTPException(status_code=404, detail="User not found.")

    if user_to_update.role == request.role:
        raise HTTPException(status_code=400, detail="The new role is the same as the current role.")

    if request.role != RoleEnum.admin and current_user_role == RoleEnum.admin and username == current_username:
        # Check if the admin is the only admin in the system
        if session.query(User).filter(User.role == RoleEnum.admin).count() == 1:
            raise HTTPException(status_code=400,
                                detail=f"You cannot change your role to {request.role.value} as it would leave the system without an admin.")

    # Update the user's role
    user_to_update.role = request.role

    # Commit the changes to the database
    session.commit()

    # Return a success message
    return JSONResponse(
        content={"message": f"User {username}'s role has been updated to {request.role.value}."},
        status_code=200
    )

class UserResponse(BaseModel):
    username: str
    email: str
    role: RoleEnum


@authRouter.get("/users/all", response_model=List[UserResponse])
async def get_users(session: SessionDep, Authorization: str = Header(...)):
    # You may want to check if the current user has the 'admin' role
    current_user_role = get_current_role_from_header(Authorization)

    if current_user_role != RoleEnum.admin:
        raise HTTPException(status_code=403, detail="Only admins can view all users.")

    # Fetch all users
    users = session.exec(select(User)).all()

    # If no users found, raise an error
    if not users:
        raise HTTPException(status_code=404, detail="No users found.")

    # Return the list of users
    return users

@authRouter.get("/users/me")
async def get_current_user(session: SessionDep, Authorization: str = Header(...)):
    # Get the current username from the token
    username = get_current_username_from_header(Authorization)

    # Fetch the user from the database using the username
    user = session.query(User).filter(User.username == username).first()

    # If the user does not exist, raise an error
    if not user:
        raise HTTPException(status_code=404, detail="User not found.")

    # Return the user data in JSONResponse
    return JSONResponse(
        content={
            "username": user.username,
            "email": user.email,
            "role": user.role.value,
        },
        status_code=200
    )


