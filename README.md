# Smart Home Automation with Voice Authentication
## Overview
Smart Home Automation with Voice Authentication is a 6-month final-year engineering project developed by a team of four engineers — two software and two electrical engineers.
The system enables users to control home appliances using secure, locally processed voice commands, ensuring privacy and data ownership without relying on third-party APIs.
The system integrates voice recognition, speech-to-text, NLP, and IoT components into a unified platform that controls real appliances through an Android application connected to a FastAPI backend and Arduino-based circuit.

## Motivation
Commercial smart home solutions often compromise user privacy by transmitting and storing audio on external servers.
This project was designed to prioritize user privacy and local processing, ensuring:
- No third-party API usage.
- All data stored and processed locally.
- Authentication strictly based on the user’s unique voice.

## System Architecture
### Main Components:
- Voice Recognition Model – Authenticates the user’s identity.
- Speech-to-Text (STT) Model – Converts valid audio to text.
- Custom NLP Model – Interprets user commands and maps them to device actions.
- Arduino Listener – Constantly listens for serial messages from the Arduino.
- Backend (FastAPI) – Handles authentication, voice processing, NLP pipeline, and device communication.
- Frontend (Android App) – Built in Kotlin, allows voice or manual appliance control.
- Database (SQLite) – Stores users, roles, command logs, and device states.
- Hardware – Raspberry Pi 5 + Arduino Mega 2560 controlling real sensors and appliances.

## Tech Stack
- Backend :	FastAPI (Python)
- Database :	SQLite
- Machine Learning :	SpeechBrain (Voice Recognition + Speech-To-Text), Scikit-Learn (Custom NLP)
- Frontend :	Kotlin (Android Studio)
- Hardware :	Raspberry Pi 5, Arduino Mega 2560
- Testing Tools :	Postman
- IDE	PyCharm : (Backend), Android Studio (Frontend), Arduino IDE (Arduino)

## Features
* Secure Voice Authentication
  * Each user records a 10-second voice sample during signup.
  * Voice is later used for identity verification before any command execution.
  * The system rejects voice commands from unauthorized users.
* User Roles and Permissions
  * Admin – Full control, manage users, check logs.
  * Family – Full control of devices, except managing users.
  * User – Limited access (e.g., lights/fan only, no voice control).
  * Guest – Read-only access (view appliance statuses).
* Voice Command Processing Pipeline
  * The app records an AAC audio file and sends it via API.
  * Backend verifies JWT token and user role.
  * Audio → WAV → Voice Recognition → STT → NLP → Command (e.g., lights_on).
  * Command sent via serial to Arduino.
  * Arduino executes action and confirms success.
  * Backend updates database only after confirmation to prevent false positives.
* Manual Control
  * Users can manually control lights, fan, heater, and door through the UI.
* Intrusion Detection
  * After 3 failed voice attempts, the system locks the feature.
  * Intruder audio is stored securely for the homeowner.
* Command Scheduling
  * “Timer” feature lets users schedule commands (e.g., “Turn on the lights at 20:00”).
* Logging and Monitoring
  * Admins can view who executed which command, when, and by what method (voice/manual/timer).

## Implementation Details
### Backend Workflow
1. Audio Upload & JWT Verification
2. AAC → WAV Conversion
3. Voice Recognition (SpeechBrain)
4. Speech-to-Text (SpeechBrain)
5. NLP Classification (scikit-learn)
6. Command Transmission (Serial to Arduino)
7. Arduino Confirmation → DB Update

### NLP Training
* Dataset located under /NLP/datasets/
* Trained on domain-specific data for flexible commands (e.g. “Turn on the lights”, “Make the room brighter”, “Please can you turn on the lights” → lights_on)

### Communication Layer
* Serial connection between Raspberry Pi and Arduino.
* Arduino sends sensor data and confirmation signals to backend listener.

## Hardware Setup
* Arduino Mega 2560 controlling appliances and sensors.
* Raspberry Pi 5 hosting the backend and serial communication.
* Physical devices: LED lights, fan, heater, door lock.

## Performance
| Metric                           | Result                                   |
| -------------------------------- | ---------------------------------------- |
| Voice command end-to-end latency | **0.64s (best)** / ~**0.88s–1.0s (avg)** |
| NLP model accuracy               | **85.85%**                               |
| External API usage               | **0 (fully local)**                      |


## Team & Contributions
| Member | Role |
|---------|------|
| Anthony Lazkani | **Backend Developer** — FastAPI backend, voice/NLP integration, database, serial communication, and API security |
| Mathieu Khoury  | **Frontend Developer** — Android App (Kotlin), UI/UX, API integration |
| Clarita Hleihel & Khaled Najem | **Electrical Engineers** — Collaboratively developed the hardware layer: circuit design using Arduino Mega 2560, sensor integration, appliance wiring, and serial communication with Raspberry Pi |



