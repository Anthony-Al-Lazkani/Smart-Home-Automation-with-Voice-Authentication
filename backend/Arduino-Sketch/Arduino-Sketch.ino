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
//     if (Serial.available() > 0) {
//         String command = Serial.readStringUntil('\n');
//         command.trim();
//         if (command == "lights_on") {
//             digitalWrite(LED_PIN, HIGH); // Turn LED ON
//             Serial.println("lights_on");
//
//         } else if (command == "lights_off") {
//             digitalWrite(LED_PIN, LOW); // Turn LED OFF
//             Serial.println("lights_off");
//
//         } else if (command == "heater_on") {
//             digitalWrite(HEATER_PIN, HIGH);
//             Serial.println("heater_on");
//         } else if (command == "heater_off") {
//             digitalWrite(HEATER_PIN, LOW);
//             Serial.println("heater_off");
//         }
//         else if (command == "door_lock") {
//             digitalWrite(DOOR_PIN, LOW);
//             Serial.println("door_lock");
//         } else if (command == "door_unlock") {
//             digitalWrite(DOOR_PIN, HIGH);
//             Serial.println("door_unlock");
//         } else {
//             Serial.println("false");
//         }
//     }
        if (Serial.available() > 0) {
        String fullCommand = Serial.readStringUntil('\n');
        fullCommand.trim();

        int sepIndex = fullCommand.indexOf('|');
        String command = fullCommand;
        String source = "manual";

        if (sepIndex != -1) {
            command = fullCommand.substring(0, sepIndex);
            source = fullCommand.substring(sepIndex + 1);
        }

        if (command == "lights_on") {
            digitalWrite(LED_PIN, HIGH);
            Serial.println("lights_on|" + source);

        } else if (command == "lights_off") {
            digitalWrite(LED_PIN, LOW);
            Serial.println("lights_off|" + source);

        } else if (command == "heater_on") {
            digitalWrite(HEATER_PIN, HIGH);
            Serial.println("heater_on|" + source);

        } else if (command == "heater_off") {
            digitalWrite(HEATER_PIN, LOW);
            Serial.println("heater_off|" + source);

        } else if (command == "door_lock") {
            digitalWrite(DOOR_PIN, LOW);
            Serial.println("door_lock|" + source);

        } else if (command == "door_unlock") {
            digitalWrite(DOOR_PIN, HIGH);
            Serial.println("door_unlock|" + source);
        } else {
            Serial.println("false");
        }
    }
}