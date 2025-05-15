from fastapi import Depends
from typing import Annotated
from database import get_session
from sqlalchemy.orm import Session
from sqlmodel import select
from models.deviceModel import Device
from datetime import datetime, timezone

from models.indicatorModel import Indicator
from models.logModel import Log, SourceEnum

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


async def update_indicator_status(indicator_name: str, indicator_status: bool,
                                  session: Session) -> bool:
    # Find the indicator by name
    indicator = session.exec(select(Indicator).where(Indicator.indicator_name == indicator_name)).first()

    if not indicator:
        return False

    # Update the indicator's status
    indicator.status = indicator_status
    indicator.last_updated = datetime.now(timezone.utc)
    session.commit()
    session.refresh(indicator)

    return True

async def create_log(user: str, command: str, source: SourceEnum, session: SessionDep):
    # Create a new log entry
    new_log = Log(
        user=user,
        command=command,
        source=source,
        issued_at=datetime.utcnow()  # Use the current UTC time for the timestamp
    )

    # Add the new log to the session and commit
    session.add(new_log)
    session.commit()
    session.refresh(new_log)


