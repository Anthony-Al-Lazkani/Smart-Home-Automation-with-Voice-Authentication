from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from database import get_session
from sqlalchemy.orm import Session
from typing import Annotated
from sqlmodel import select
from models.deviceModel import Device
from pydantic import BaseModel
from datetime import datetime, timezone
from serialCommunicationUtils import send_message


deviceManagementRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

class DeviceStatusRequest(BaseModel):
    status: bool

class DeviceCreateRequest(BaseModel):
    device_name: str

device_action_mapping = {
    "lights_on": ("lights", True),
    "lights_off": ("lights", False),
    "heater_on": ("heater", True),
    "heater_off": ("heater", False),
    "door_lock": ("door", False),
    "door_unlock": ("door", True)
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


# Control a device manually
# @deviceManagementRouter.post("/device/{command}")
# async def manual_control(session: SessionDep, command : str):
#     arduino_response = send_message(command)
#
#     if not arduino_response:
#         raise HTTPException(status_code=400, detail="Arduino could not execute the requested command")
#
#     device_info = device_action_mapping.get(command)
#     if not device_info:
#         return JSONResponse(
#             content={"message": "Invalid command", "isAuthenticated": True},
#             status_code=400
#         )
#     device_name, device_status = device_info
#     response = await update_device_status(device_name, device_status, session)
#
#     if not response:
#         raise HTTPException(status_code=404, detail="Device not found")
#
#     return JSONResponse(status_code=200, content={"message" : "Device updated successfully"})


@deviceManagementRouter.post("/device/{command}")
async def manual_control(command : str):
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

