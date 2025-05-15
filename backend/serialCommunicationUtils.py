import serial
import time
import threading
from deviceManagementUtils import update_device_status, update_indicator_status
import asyncio
from database import get_session
from sqlalchemy.orm import Session

from timerManagementUtils import reset_device_timer_field

ser: serial.Serial | None = None
# last_command_sent: str | None = None
listener_running = True

device_action_mapping = {
    "lights_on": ("lights", True),
    "lights_off": ("lights", False),
    "heater_on": ("heater", True),
    "heater_off": ("heater", False),
    "door_lock": ("door", False),
    "door_unlock": ("door", True),
    "fan_on": ("fan", True),
    "fan_off": ("fan", False),
    "fan_high" : ("fan", True),
    "security_on" : ("security", True),
    "security_off" : ("security", False),
    "ldr_on" : ("ldr", True),
    "ldr_off" : ("ldr", False)
}

indicator_action_mapping = {
    "security_on": ("security", True),
    "security_off": ("security", False),
    "fire_on" : ("fire", True),
    "fire_off" : ("fire", False),
    "gas_on" : ("gas", True),
    "gas_off" : ("gas", False),
    "earthquake_on" : ("earthquake", True),
    "earthquake_off" : ("earthquake", False),
}

def open_serial_connection(port='/dev/ttyUSB0', baudrate=9600, timeout=1.0):
    """
    Opens the serial connection and returns the serial object.
    """
    global ser
    if ser is None or not ser.is_open:
        ser = serial.Serial(port, baudrate, timeout=timeout)
        ser.reset_input_buffer()
        print("✅ Serial connection opened.")
    return ser

def close_serial_connection():
    """
    Closes the serial connection if open.
    """
    global ser
    if ser is not None and ser.is_open:
        ser.close()
        print("🛑 Serial connection closed.")
    else:
        print("⚠️ Serial connection is not open or already closed.")



def send_message(command: str, source: str = "manual"):
    global ser

    if ser is None or not ser.is_open:
        print("⚠️ Serial connection is not open. Opening connection...")
        open_serial_connection()

    try:
        # Add metadata so we know the source of the command
        time.sleep(2)
        ser.write(f"{command}|{source}\n".encode('utf-8'))
        print(f"Sent command: {command} (source: {source})")

    except serial.SerialException as e:
        print(f"Error while sending message: {e}")

def serial_listener():
    global ser
    open_serial_connection()
    print("🔄 Serial listener running...")

    session_gen = get_session()
    session = next(session_gen)

    try:
        while listener_running:
            if ser.in_waiting > 0:
                response = ser.readline().decode('utf-8').strip()
                print(f"📥 Received from Arduino: {response}")

                # Run the async function from a sync thread
                asyncio.run(handle_arduino_response(response, session))
    finally:
        session.close()


async def handle_arduino_response(response: str, session: Session):
    # global last_command_sent

    # Parse command and source
    if "|" in response:
        command, source = response.split("|", 1)
    else:
        command = response
        source = "manual"  # default

    # Handle source from Arduino: INDICATORS
    if source == "arduino":
        if command not in indicator_action_mapping:
            print(f"⚠️ Unrecognized indicator command from Arduino: {command}")
            return

        indicator_name, indicator_status = indicator_action_mapping[command]
        print(f"📡 [Arduino] Updating indicator '{indicator_name}' to {indicator_status}")
        await update_indicator_status(indicator_name, indicator_status, session)
        return

    # Handle source from Timer: DEVICES
    if source == "timer":
        if command not in device_action_mapping:
            print(f"⚠️ Unrecognized timer command: {command}")
            return

        device_name, device_status = device_action_mapping[command]
        print(f"⏰ [Timer] Updating device '{device_name}' to {device_status}")
        await update_device_status(device_name, device_status, session)

        # Reset scheduled times
        if "on" in command:
            await reset_device_timer_field(device_name, "on_time", session)
        elif "off" in command:
            await reset_device_timer_field(device_name, "off_time", session)

        # last_command_sent = None
        return  # Done

    # Handle source from Manual: DEVICES
    if source == "manual":
        if command not in device_action_mapping:
            print(f"⚠️ Unrecognized manual command: {command}")
            return

        device_name, device_status = device_action_mapping[command]
        print(f"🧑 [Manual] Updating device '{device_name}' to {device_status}")
        await update_device_status(device_name, device_status, session)
        # last_command_sent = None
        return

def start_listener_thread():
    listener_thread = threading.Thread(target=serial_listener, daemon=True)
    listener_thread.start()
    print("🚀 Serial listener thread started.")

def stop_listener():
    global listener_running
    listener_running = False  # This will stop the while loop in the serial_listener function
    close_serial_connection()  # Ensure the serial connection is closed
    print("🛑 Listener stopped and serial connection closed.")
