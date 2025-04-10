import serial
import time

ser: serial.Serial | None = None

def open_serial_connection(port='/dev/ttyACM1', baudrate=9600, timeout=1.0):
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


def send_message(command: str) -> bool:
    """
    Sends a command to the Arduino via the serial connection.
    """
    global ser
    if ser is None or not ser.is_open:
        print("⚠️ Serial connection is not open. Opening connection...")
        open_serial_connection()  # Ensure the serial connection is open

    try:
        # Send the command to Arduino
        time.sleep(2)
        ser.write(f"{command}\n".encode('utf-8'))
        print(f"Sent command: {command}")

        # Optionally, read the response from the Arduino (if any)
        response = ser.readline().decode('utf-8').strip()
        if response == "true":
            return True
        elif response == "false":
            return False

    except serial.SerialException as e:
        print(f"Error while sending message: {e}")

