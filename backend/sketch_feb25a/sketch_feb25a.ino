#include <ESP8266WiFi.h>

const char* ssid = "Maro";
const char* password = "Marmoura@70956560";

WiFiServer server(80);

void setup() {
  delay(1000); 
  pinMode(D0, OUTPUT);
  digitalWrite(D0, LOW);

  Serial.begin(115200);
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nNodeMCU is connected to WiFi");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  
  server.begin();
}

void loop() {
  WiFiClient client = server.available();
  
  if (client) {
    Serial.println("New Client Connected");

    // Wait for the client to send data
    while (!client.available()) {
      delay(1);
    }

    // Read the first line of the request
    String request = client.readStringUntil('\r');
    client.read();  // Consume newline

    Serial.println("Request: " + request);

    // Ignore favicon requests
    if (request.indexOf("favicon.ico") != -1) {
      Serial.println("Ignoring favicon request");
      client.stop();
      return;
    }

    // Control LED based on request
    if (request.indexOf("lights_on") != -1 || request.indexOf("turn on the lights") != -1) {
      digitalWrite(D0, HIGH);
      Serial.println("LED is ON");
    }

    if (request.indexOf("lights_off") != -1 || request.indexOf("turn off the lights") != -1) {
      digitalWrite(D0, LOW);
      Serial.println("LED is OFF");
    }



    Serial.println("Client Disconnected\n------------------");
    client.stop();
  }
}