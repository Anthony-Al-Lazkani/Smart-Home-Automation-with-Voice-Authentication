#define LED_PIN 40  // Define pin 40 for LED

void setup() {
    pinMode(LED_PIN, OUTPUT);
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
        } else {
            Serial.println("false");
        }
    }
}