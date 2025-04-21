from fastapi import Depends
from typing import Annotated
from database import get_session
from sqlalchemy.orm import Session
from sqlmodel import select
from models.deviceModel import Device
from datetime import datetime, timezone

SessionDep = Annotated[Session, Depends(get_session)]


async def update_device_status(device_name: str, device_status: bool,
                               session: SessionDep) -> bool:
    # Find device by name
    device = session.exec(select(Device).where(Device.device_name == device_name)).first()

    if not device:
        return False

    # Update the device status
    device.status = device_status
    device.last_updated = datetime.now(timezone.utc)  # Update timestamp
    session.commit()
    session.refresh(device)

    return True