from fastapi import APIRouter, HTTPException, Depends, BackgroundTasks
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from sqlmodel import Session, select
from models.timerModel import DeviceTimer
from database import get_session
from typing import Annotated, Optional
from fastapi.encoders import jsonable_encoder
import time
from datetime import datetime, timedelta
import asyncio
from jwt_utils import get_current_username
from models.userModel import RoleEnum
from serialCommunicationUtils import send_message

timerRouter = APIRouter()

SessionDep = Annotated[Session, Depends(get_session)]

USER_ALLOWED_DEVICES = {"lights", "fan"}

class DeviceTimerRequest(BaseModel):
    device_type : str

# # Add a new device
@timerRouter.post("/")
async def add_device(device_timer: DeviceTimerRequest, session: SessionDep) -> JSONResponse:
    # Check if device already exists
    existing_device_timer = session.exec(select(DeviceTimer).where(DeviceTimer.device_type == device_timer.device_type)).first()

    if existing_device_timer:
        raise HTTPException(status_code=400, detail="Device already exists")

    # Create new device
    new_device_timer = DeviceTimer(
        device_type=device_timer.device_type,
        on_time=None,
        off_time=None
    )

    session.add(new_device_timer)
    session.commit()
    session.refresh(new_device_timer)

    return JSONResponse(
        content={"message": f"Device {device_timer.device_type} added successfully"},
        status_code=201
    )

# def control_device(device_type: str, action: str):
#     print(f"Action '{action}' triggered for device: {device_type}")

def calculate_time_difference(action_time: str) -> int:
    current_time = datetime.now()
    action_time = datetime.strptime(action_time, "%H:%M").replace(year=current_time.year, month=current_time.month, day=current_time.day)

    # If the action time has already passed today, schedule it for the next day
    if action_time < current_time:
        action_time += timedelta(days=1)

    time_diff = (action_time - current_time).total_seconds()
    return int(time_diff)


async def sleep_and_trigger_action(device_type: str, action: str, action_time: str):
    # Calculate the delay until the action time
    time_diff = calculate_time_difference(action_time)

    # Sleep for the calculated amount of time before triggering the action
    # time.sleep(time_diff)
    await asyncio.sleep(time_diff)
    # Dictionary mapping device types and actions to their respective commands
    device_commands = {
        "lights": {
            "Turn on": "lights_on",
            "Turn off": "lights_off"
        },
        "heater": {
            "Turn on": "heater_on",
            "Turn off": "heater_off"
        },
        "fan": {
            "Turn on": "fan_on",
            "Turn off": "fan_off"
        }
    }

    # Check if the device type exists in the dictionary
    if device_type in device_commands:
        # Check if the action exists for the given device type
        if action in device_commands[device_type]:
            command = device_commands[device_type][action]
            send_message(command, source="timer")
        else:
            print(f"Unknown action '{action}' for device type '{device_type}'")
    else:
        print(f"Unknown device type: {device_type}")


# Set new timer
class SetDeviceTimerRequest(BaseModel):
    token : str
    on_time : Optional[str] = None
    off_time : Optional[str] = None

@timerRouter.put("/{device_type}")
async def update_device_timer(
    device_type: str,
    device_timer: SetDeviceTimerRequest,
    session: SessionDep,
    background_tasks: BackgroundTasks
) -> JSONResponse:

    current_username = get_current_username(device_timer.token)
    current_user = session.exec(select(User).where(User.username == current_username))

    if current_user.role == RoleEnum.guest:
        raise HTTPException(status_code=403, detail="Guests are not allowed to set timer for any device"
        )

    if current_user.role == RoleEnum.user and command not in USER_ALLOWED_DEVICES:
        raise HTTPException(
            status_code=403,
            detail="Users are not allowed to set timer for this specific device"
        )

    # Check if the device exists
    existing_device_timer = session.exec(select(DeviceTimer).where(DeviceTimer.device_type == device_type)).first()

    if not existing_device_timer:
        raise HTTPException(status_code=404, detail="Device not found")

    # Validate at least one of on_time or off_time is provided
    if not device_timer.on_time and not device_timer.off_time:
        raise HTTPException(status_code=400, detail="At least one of 'on_time' or 'off_time' must be provided")


    if device_timer.on_time:
        existing_device_timer.on_time = device_timer.on_time
        action_time = device_timer.on_time
        # Schedule the action to be triggered in the background
        background_tasks.add_task(sleep_and_trigger_action, device_type, "Turn on", action_time)

    if device_timer.off_time:
        existing_device_timer.off_time = device_timer.off_time
        action_time = device_timer.off_time
        # Schedule the action to be triggered in the background
        background_tasks.add_task(sleep_and_trigger_action, device_type, "Turn off", action_time)

    session.commit()
    session.refresh(existing_device_timer)

    return JSONResponse(
        content={"message": f"Device timer for {device_type} updated successfully"},
        status_code=200
    )



# Get all devices
@timerRouter.get("/timers")
async def get_all_devices_timers(session: SessionDep) -> JSONResponse:
    device_timers = session.exec(select(DeviceTimer)).all()

    if not device_timers:
        raise HTTPException(status_code=404, detail="No device timers found")

    serialized_devices = jsonable_encoder(device_timers)

    return JSONResponse(
        content={"device_timers": serialized_devices},
        status_code=200
    )

# Resets device type to default value
@timerRouter.put("/reset/{device_type}")
async def reset_device_timer(
    device_type: str,
    session: SessionDep
) -> JSONResponse:
    # Check if the device exists
    existing_device_timer = session.exec(select(DeviceTimer).where(DeviceTimer.device_type == device_type)).first()

    if not existing_device_timer:
        raise HTTPException(status_code=404, detail="Device not found")

    # Reset the on_time and off_time to None
    existing_device_timer.on_time = None
    existing_device_timer.off_time = None

    session.commit()
    session.refresh(existing_device_timer)

    return JSONResponse(
        content={"message": f"Device timer for {device_type} has been reset successfully"},
        status_code=200
    )


