#define LED_PIN 35
#define HEATER_PIN 28
#define DOOR_PIN 32
#define SECURITY_PIN 31

#define LED_PIR_PIN 27
#define PIR_PIN 47

#define MOTOR_PIN_1 2
#define MOTOR_PIN_2 3
#define ENABLE_PIN 9

#define PIN_ON_HIGH 8
#define PIN_OFF 6
#define PIN_ON_NORMAL 5

#define LDR_SENSOR A6
#define LDR_PIN 50
#define LDR_TOGGLE 52
#define LDR_THRESHOLD 300
#define GAS_THRESHOLD 200
#define NUM_MEASUREMENTS 15

#define GAS_SENSOR A8
#define GAS_PIN 45

#define FLAME_SENSOR_PIN 29
#define PUMP_PIN 22

#define VIBRATION_SENSOR_PIN 25
#define VIBRATION_BUZZER_PIN 41

bool pirDetection = false;
bool gasDetection = false;
bool fireDetection = false;
bool earthquakeDetection = false;
unsigned long pirLastDetectedTime = 0;
unsigned long gasLastDetectedTime = 0;
unsigned long fireLastDetectedTime = 0;
unsigned long earthquakeLastDetectedTime = 0;
unsigned long gasOffStart = 0;


unsigned long fireOffStart = 0;



void setup() {
    pinMode(LED_PIN, OUTPUT);
    pinMode(HEATER_PIN, OUTPUT);
    pinMode(DOOR_PIN, OUTPUT);
    pinMode(SECURITY_PIN,INPUT);
    pinMode(FLAME_SENSOR_PIN,INPUT);
    pinMode(PUMP_PIN,OUTPUT);
    pinMode(PIR_PIN,INPUT);
    pinMode(LED_PIR_PIN,OUTPUT);
    pinMode(LDR_PIN,OUTPUT);
    pinMode(LDR_TOGGLE,OUTPUT);

    pinMode(MOTOR_PIN_1, OUTPUT);
    pinMode(MOTOR_PIN_2, OUTPUT);
    pinMode(ENABLE_PIN, OUTPUT);
    pinMode(PIN_ON_HIGH, OUTPUT);
    pinMode(PIN_OFF, OUTPUT);
    pinMode(PIN_ON_NORMAL, OUTPUT);
    pinMode(VIBRATION_SENSOR_PIN, INPUT);
    pinMode(VIBRATION_BUZZER_PIN, OUTPUT);

    pinMode(GAS_PIN, OUTPUT);

    digitalWrite(PUMP_PIN, LOW);
    digitalWrite(VIBRATION_BUZZER_PIN, LOW);
    Serial.begin(9600);
}

void loop() {

    String source_arduino = "arduino";

    int ldrTotal = 0;
    int gasTotal = 0;
    for (int i = 0; i < NUM_MEASUREMENTS ; i++) {
        ldrTotal += analogRead(LDR_SENSOR);
        gasTotal += analogRead(GAS_SENSOR);
        delay(5);
    }

    int ldrAverage = ldrTotal / NUM_MEASUREMENTS;
    int gasAverage = gasTotal / NUM_MEASUREMENTS;


    if (digitalRead(LDR_TOGGLE) == HIGH) {
        if (ldrAverage < LDR_THRESHOLD) {
            digitalWrite(LDR_PIN, HIGH);
        } else {
            digitalWrite(LDR_PIN, LOW);
        }
    }


    if (gasAverage > GAS_THRESHOLD) {
        if (!gasDetection) {
            digitalWrite(GAS_PIN, HIGH);
            gasDetection = true;
            gasLastDetectedTime = millis();
            Serial.println("gas_on|" + source_arduino);
        } else {

        }
    } else if (gasDetection && millis() - gasLastDetectedTime >= 7000) {
        digitalWrite(GAS_PIN, LOW);
        gasDetection = false;
        Serial.println("gas_off|" + source_arduino);
    }


    int sensorValue = digitalRead(PIR_PIN);
        int SecurityStatus = digitalRead(SECURITY_PIN);


    if (sensorValue == HIGH && SecurityStatus == HIGH) {
    if (!pirDetection) {
        digitalWrite(LED_PIR_PIN, HIGH);
        pirDetection = true;
        pirLastDetectedTime = millis();
        Serial.println("security_on|" + source_arduino);
    } else {
        // Motion is still detected, do nothing
    }
    } else if (pirDetection && millis() - pirLastDetectedTime >= 10000) {
    digitalWrite(LED_PIR_PIN, LOW);
    pirDetection = false;
    Serial.println("security_off|" + source_arduino);
    }



    int fireSensorValue = digitalRead(FLAME_SENSOR_PIN);
    if (fireSensorValue == HIGH) {
    if (!fireDetection) {
        digitalWrite(PUMP_PIN, HIGH);
        fireDetection = true;
        fireLastDetectedTime = millis();
        Serial.println("fire_on|" + source_arduino);
    } else {
        // Fire is still detected, do nothing
    }
    } else if (fireDetection && millis() - fireLastDetectedTime >= 7000) {
            digitalWrite(PUMP_PIN, LOW);
            fireDetection = false;
            Serial.println("fire_off|" + source_arduino);
    }

    int vibrationSensorValue = digitalRead(VIBRATION_SENSOR_PIN);
    if (vibrationSensorValue == HIGH) {
    if (!earthquakeDetection) {
        digitalWrite(VIBRATION_BUZZER_PIN, HIGH);
        earthquakeDetection = true;
        earthquakeLastDetectedTime = millis();
        Serial.println("earthquake_on|" + source_arduino);
    } else {
        // Fire is still detected, do nothing
    }
    } else if (earthquakeDetection && millis() - earthquakeLastDetectedTime >= 7000) {
            digitalWrite(VIBRATION_BUZZER_PIN, LOW);
            earthquakeDetection = false;
            Serial.println("earthquake_off|" + source_arduino);
    }



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
        } else if (command == "fan_on") { 
            digitalWrite(MOTOR_PIN_1, HIGH);
            digitalWrite(MOTOR_PIN_2, LOW);
            analogWrite(ENABLE_PIN, 60);
            Serial.println("fan_on|" + source);
        }else if (command == "fan_off") {  
            digitalWrite(MOTOR_PIN_1, LOW);
            digitalWrite(MOTOR_PIN_2, LOW);
            analogWrite(ENABLE_PIN, 0);
            Serial.println("fan_off|" + source);
        } else if (command == "fan_high") {  
            digitalWrite(MOTOR_PIN_1, HIGH);
            digitalWrite(MOTOR_PIN_2, LOW);
            analogWrite(ENABLE_PIN, 255);

        } else if (command == "security_on") {  
            digitalWrite(SECURITY_PIN, HIGH);
            Serial.println("security_on|" + source);
        } else if (command == "security_off") {  
            digitalWrite(SECURITY_PIN, LOW);
            Serial.println("security_off|" + source);
            Serial.println("security_off|" + source_arduino);
        } else if (command == "ldr_on") {  
            digitalWrite(LDR_TOGGLE, HIGH);
            Serial.println("ldr_on|" + source);
        } else if (command == "ldr_off") {  
            digitalWrite(LDR_TOGGLE, LOW);
            Serial.println("ldr_off|" + source);
        }else {
            Serial.println("false");
        }
    }
}