#define LED_PIN 40 
#define HEATER_PIN 50
#define DOOR_PIN 22

void setup() {
    pinMode(LED_PIN, OUTPUT);
    pinMode(HEATER_PIN, OUTPUT);
    pinMode(DOOR_PIN, OUTPUT);
    Serial.begin(9600);
}

void loop() {
    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        command.trim();
        if (command == "lights_on") {
            digitalWrite(LED_PIN, HIGH); // Turn LED ON
            Serial.println("true");
        } else if (command == "lights_off") {
            digitalWrite(LED_PIN, LOW); // Turn LED OFF
            Serial.println("true");
        } else if (command == "heater_on") {
            digitalWrite(HEATER_PIN, HIGH);
            Serial.println("true");
        } else if (command == "heater_off") {
            digitalWrite(HEATER_PIN, LOW);
            Serial.println("true");
        }
        else if (command == "door_lock") {
            digitalWrite(DOOR_PIN, LOW);
            Serial.println("true");
        } else if (command == "door_unlock") {
            digitalWrite(DOOR_PIN, HIGH);
            Serial.println("true");
        } else {
            Serial.println("false");
        }
    }
}