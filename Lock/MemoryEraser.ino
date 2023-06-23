#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <EEPROM.h>
#include <string.h>
#include <ArduinoJson.h>

// Define pin names for NodeMCU V3 board
#define RED_LED 0   // GPIO0, output pin for internal red LED (Solenoid relay) (Reverse)
#define BLUE_LED 2  // GPIO2, output pin for internal blue LED (External LED) (Reverse)
#define KP_R1 14    // GPIO14, input pin from Keypad for row 1
#define KP_R2 12    // GPIO12, input pin from Keypad for row 2
#define KP_R3 13    // GPIO13, input pin from Keypad for row 3
#define KP_R4 15    // GPIO15, input pin from Keypad for row 4
#define KP_C1 16    // GPIO16, input pin from Keypad for column 1
#define KP_C2 5     // GPIO5, input pin from Keypad for column 2
#define KP_C3 4     // GPIO4, input pin from Keypad for column 3


// Variable addresses
const int LOCK_ID_ADDRESS = 0;
const int USER_ID_ADDRESS = 50;
const int WIFI_NAME_ADDRESS = 100;
const int WIFI_PASS_ADDRESS = 150;
const int MAX_STRING_SIZE = 50;
const String HOTSPOT_SSID = "LokiTech";
const String API_URL = "https://api-endpoint.com";


// Global Variables
String lockId;
String userId;
String wifiName;
String wifiPass;
WiFiClient wifi_client;

void setup() {
  // Serial.begin(9600);
  EEPROM.begin(512);

  // Set pin modes (Outputs)
  pinMode(RED_LED, OUTPUT);
  pinMode(BLUE_LED, OUTPUT);

  // Set pin modes (Inputs)
  pinMode(KP_R1, INPUT);
  pinMode(KP_R2, INPUT);
  pinMode(KP_R3, INPUT);
  pinMode(KP_R4, INPUT);
  pinMode(KP_C1, INPUT);
  pinMode(KP_C2, INPUT);
  pinMode(KP_C3, INPUT);

  digitalWrite(RED_LED, HIGH);
  digitalWrite(BLUE_LED, LOW);

  lockId = "3";
  userId = "3";
  wifiName = "SadraM";
  wifiPass = "12345678";

  writeStringToEEPROM(LOCK_ID_ADDRESS, lockId);
  writeStringToEEPROM(USER_ID_ADDRESS, userId);
  writeStringToEEPROM(WIFI_NAME_ADDRESS, wifiName);
  writeStringToEEPROM(WIFI_PASS_ADDRESS, wifiPass);
}

void loop() {
  delay(1000);
  digitalWrite(BLUE_LED, LOW);
  delay(1000);
  digitalWrite(BLUE_LED, HIGH);
}

// Reads a string from the EEPROM memory starting at the specified address
String readStringFromEEPROM(int address) {
  char buffer[MAX_STRING_SIZE];
  int i;
  for (i = 0; i < MAX_STRING_SIZE; i++) {
    char c = EEPROM.read(address + i);
    if (c == 0) {
      break;
    }
    buffer[i] = c;
  }
  buffer[i] = '\0';
  return String(buffer);
}

// Writes a string to the EEPROM memory starting at the specified address
void writeStringToEEPROM(int address, String value) {
  int length = value.length();
  for (int i = 0; i < length; i++) {
    EEPROM.write(address + i, value.charAt(i));
  }
  EEPROM.write(address + length, '\0');
  EEPROM.commit();
}
