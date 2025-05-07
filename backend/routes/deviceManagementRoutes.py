from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from database import get_session
from sqlalchemy.orm import Session
from typing import Annotated, List
from sqlmodel import select
from models.deviceModel import Device
from models.indicatorModel import Indicator
from pydantic import BaseModel
from datetime import datetime, timezone
from models.userModel import User

from models.userModel import RoleEnum
from serialCommunicationUtils import send_message
from jwt_utils import get_current_username

deviceManagementRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

class DeviceStatusRequest(BaseModel):
    status: bool

class DeviceCreateRequest(BaseModel):
    device_name: str

USER_ALLOWED_COMMANDS = {
    "lights_on",
    "lights_off",
    "fan_on",
    "fan_off"
}

# Add a new device
@deviceManagementRouter.post("/device")
async def add_device(device: DeviceCreateRequest, session: SessionDep) -> JSONResponse:
    # Check if device already exists
    existing_device = session.exec(select(Device).where(Device.device_name == device.device_name)).first()

    if existing_device:
        raise HTTPException(status_code=400, detail="Device already exists")

    # Create new device
    new_device = Device(
        device_name=device.device_name,
        status=False,
        last_updated=datetime.now(timezone.utc)
    )

    session.add(new_device)
    session.commit()
    session.refresh(new_device)

    return JSONResponse(
        content={"message": f"Device {device.device_name} added successfully"},
        status_code=201
    )


# Delete a device
@deviceManagementRouter.delete("/device/{device_name}")
async def delete_device(device_name: str, session: SessionDep) -> JSONResponse:
    # Find device by name
    device = session.exec(select(Device).where(Device.device_name == device_name)).first()

    if not device:
        raise HTTPException(status_code=404, detail="Device not found")

    session.delete(device)
    session.commit()

    return JSONResponse(
        content={"message": f"Device {device_name} deleted successfully"},
        status_code=200
    )



# Get all devices
@deviceManagementRouter.get("/devices")
async def get_all_devices(session: SessionDep) -> JSONResponse:
    devices = session.exec(select(Device)).all()

    if not devices:
        raise HTTPException(status_code=404, detail="No devices found")

    serialized_devices = jsonable_encoder(devices)

    return JSONResponse(
        content={"devices": serialized_devices},
        status_code=200
    )


# Get device by name
@deviceManagementRouter.get("/device/{device_name}")
async def get_device(device_name: str, session: SessionDep) -> JSONResponse:
    # Find device by name
    device = session.exec(select(Device).where(Device.device_name == device_name)).first()

    if not device:
        raise HTTPException(status_code=404, detail="Device not found")

    return JSONResponse(
        content={"device": device.dict()},
        status_code=200
    )

# Control a device
class TokenBody(BaseModel):
    token : str

@deviceManagementRouter.post("/device/{command}")
async def manual_control(session : SessionDep, command : str, body : TokenBody):
    current_username = get_current_username(body.token)
    current_user = session.exec(select(User).where(User.username == current_username)).first()

    if current_user.role == RoleEnum.guest:
        raise HTTPException(
            status_code=403,
            detail="Guests are not allowed to manually control any device"
        )

    if current_user.role == RoleEnum.user and command not in USER_ALLOWED_COMMANDS:
        raise HTTPException(
            status_code=403,
            detail="Users are not allowed to manually control this device"
        )
    send_message(command)
    return JSONResponse(status_code=200, content={"message" : "Device updated successfully"})

@deviceManagementRouter.get("/device-summary")
async def get_device_summary(session: SessionDep) -> JSONResponse:
    devices = session.exec(select(Device)).all()

    if not devices:
        raise HTTPException(status_code=404, detail="No devices found")

    total_devices = len(devices)
    devices_on = sum(1 for device in devices if device.status)
    percentage_on = (devices_on / total_devices) if total_devices > 0 else 0

    return JSONResponse(
        content={
            "total_devices": total_devices,
            "devices_on": devices_on,
            "percentage_on": percentage_on
        },
        status_code=200
    )


# INDICATORS
class CreateIndicatorRequest(BaseModel):
    indicator_name: str

@deviceManagementRouter.post("/indicator")
async def create_indicator(session: SessionDep, request : CreateIndicatorRequest):

    query = select(Indicator).where(Indicator.indicator_name == request.indicator_name)
    existing_indicator = session.exec(query).first()

    if existing_indicator:
        raise HTTPException(status_code=400, detail="Indicator with this name already exists.")

    indicator = Indicator(
        indicator_name = request.indicator_name,
        status = False,
        last_updated=datetime.now(timezone.utc)
    )

    session.add(indicator)
    session.commit()
    session.refresh(indicator)

    return indicator


# GET all indicators
class IndicatorsResponse(BaseModel):
    id : int
    indicator_name : str
    status : bool
    last_updated: datetime

    class Config:
        from_attributes = True


@deviceManagementRouter.get("/indicators", response_model=List[IndicatorsResponse])
async def get_all_indicators(session : SessionDep):
    indicators = session.exec(select(Indicator)).all()
    return indicators