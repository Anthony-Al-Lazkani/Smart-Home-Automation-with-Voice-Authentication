from fastapi import Depends
from sqlmodel import select
from models.timerModel import DeviceTimer
from typing import Annotated
from database import get_session
from sqlmodel import Session

SessionDep = Annotated[Session, Depends(get_session)]

# function that reset either on time or off time in the device timer field
async def reset_device_timer_field(device_type: str, field: str, session: SessionDep) -> bool:

    # Check if the device exists
    existing_device_timer = session.exec(select(DeviceTimer).where(DeviceTimer.device_type == device_type)).first()

    if not existing_device_timer:
        return False

    # Reset the specified field
    if field == "on_time":
        existing_device_timer.on_time = None
    elif field == "off_time":
        existing_device_timer.off_time = None

    # Commit the changes to the database
    session.commit()
    session.refresh(existing_device_timer)

    return True
