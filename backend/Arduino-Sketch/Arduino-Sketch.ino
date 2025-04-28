#define LED_PIN 40 
#define HEATER_PIN 50
#define DOOR_PIN 22

#define MOTOR_PIN_1 2
#define MOTOR_PIN_2 3
#define ENABLE_PIN 9

#define PIN_ON_HIGH 8
#define PIN_OFF 6
#define PIN_ON_NORMAL 5

void setup() {
    pinMode(LED_PIN, OUTPUT);
    pinMode(HEATER_PIN, OUTPUT);
    pinMode(DOOR_PIN, OUTPUT);

    pinMode(MOTOR_PIN_1, OUTPUT);
    pinMode(MOTOR_PIN_2, OUTPUT);
    pinMode(ENABLE_PIN, OUTPUT);
    pinMode(PIN_ON_HIGH, OUTPUT);
    pinMode(PIN_OFF, OUTPUT);
    pinMode(PIN_ON_NORMAL, OUTPUT);
    Serial.begin(9600);
}

void loop() {

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
        }  else if (command == "fan_on") { 
        // Normal speed (only if OFF and high are  LOW)
        digitalWrite(MOTOR_PIN_1, HIGH);
        digitalWrite(MOTOR_PIN_2, LOW);
        analogWrite(ENABLE_PIN, 60);
        Serial.println("fan_on|" + source);
        }else if (command == "fan_off") {  
        // Turn motor off (highest priority)
        digitalWrite(MOTOR_PIN_1, LOW);
        digitalWrite(MOTOR_PIN_2, LOW);
        analogWrite(ENABLE_PIN, 0);
        Serial.println("fan_off|" + source);
        } else if (command == "fan_high") {  
        // High speed (only if OFF and normal are LOW)
        digitalWrite(MOTOR_PIN_1, HIGH);
        digitalWrite(MOTOR_PIN_2, LOW);
        analogWrite(ENABLE_PIN, 255);
        }else {
            Serial.println("false");
        }
    }
}