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
const String API_URL = "http://shacks.pythonanywhere.com/";
const byte ROWS = 4; //four rows
const byte COLS = 3; //three columns
char keys[ROWS][COLS] = {
  {'1','2','3'},   
  {'4','5','6'},   
  {'7','8','9'},   
  {'*','0','#'} 
};
byte rowPins[ROWS] = {KP_R1, KP_R2, KP_R3, KP_R4}; //connect to the row pinouts of the keypad
byte colPins[COLS] = {KP_C1, KP_C2, KP_C3}; //connect to the column pinouts of the keypad


// Global Variables
String lockId;
String userId;
String wifiName;
String wifiPass;
WiFiClient wifi_client;
char input[4] = {'1', '1', '1', '1'};
int input_cnt = 0;

void setup() {
  Serial.begin(9600);
  EEPROM.begin(512);

  // Set pin modes (Outputs)
  pinMode(RED_LED, OUTPUT);
  pinMode(BLUE_LED, OUTPUT);

  pinMode(KP_R1, OUTPUT);
  pinMode(KP_R2, OUTPUT);
  pinMode(KP_R3, OUTPUT);
  pinMode(KP_R4, OUTPUT);
  pinMode(KP_C1, INPUT_PULLUP);
  pinMode(KP_C2, INPUT_PULLUP);
  pinMode(KP_C3, INPUT_PULLUP);

  digitalWrite(RED_LED, LOW);
  digitalWrite(BLUE_LED, HIGH);

  // Check if the lock ID is stored in the EEPROM
  String lockId = readStringFromEEPROM(LOCK_ID_ADDRESS);
  if (lockId == "") {
    Serial.println("Lock ID not found, initializing to null value");
    lockId = "null";
    writeStringToEEPROM(LOCK_ID_ADDRESS, lockId);
  }
  Serial.print("Lock ID: ");
  Serial.println(lockId);

  // Check if the user ID is stored in the EEPROM
  String userId = readStringFromEEPROM(USER_ID_ADDRESS);
  if (userId == "") {
    Serial.println("User ID not found, initializing to null value");
    userId = "null";
    writeStringToEEPROM(USER_ID_ADDRESS, userId);
  }
  Serial.print("User ID: ");
  Serial.println(userId);

  // Check if the WiFi name is stored in the EEPROM
  String wifiName = readStringFromEEPROM(WIFI_NAME_ADDRESS);
  if (wifiName == "") {
    Serial.println("WiFi name not found, initializing to null value");
    wifiName = "null";
    writeStringToEEPROM(WIFI_NAME_ADDRESS, wifiName);
  }
  Serial.print("WiFi name: ");
  Serial.println(wifiName);

  // Check if the WiFi password is stored in the EEPROM
  String wifiPass = readStringFromEEPROM(WIFI_PASS_ADDRESS);
  if (wifiPass == "") {
    Serial.println("WiFi password not found, initializing to null value");
    wifiPass = "null";
    writeStringToEEPROM(WIFI_PASS_ADDRESS, wifiPass);
  }
  Serial.print("WiFi password: ");
  Serial.println(wifiPass);

  digitalWrite(BLUE_LED, LOW);
  if (wifiPass == "null" && wifiName == "null" && lockId == "null" && userId == "null") {
    while (!firstConfig()) {
      continue;
    }
    sendLog(5);
  }

  Serial.print("Connecting to ");
  Serial.println(wifiName);
  WiFi.begin(wifiName, wifiPass);

  int tries = 0;

  while (WiFi.status() != WL_CONNECTED && tries < 15) {
    delay(1000);
    Serial.print(".");
    tries++;
  }

  if (WiFi.status() != WL_CONNECTED){
    Serial.println("");
    Serial.println("WiFi connection error!");    
    while (!changeWifi()){
      Serial.println("WiFi changing ...");
    }
    
  }

  Serial.println("");
  Serial.println("WiFi connected");

  digitalWrite(BLUE_LED, HIGH);
  delay(1000);
  digitalWrite(BLUE_LED, LOW);
  Serial.println("Going to loop");
  input[0] = '*';
  input[1] = '*';
  input[2] = '*';
  input[3] = '*';
  input_cnt = 0;
}

char getKey(){
  for (int row = 0; row < ROWS; row++){
    digitalWrite(rowPins[row], HIGH);
    for (int col = 0; col < COLS; col++){
      if (digitalRead(colPins[col]) == HIGH){
        Serial.print("Key ");
        Serial.print(keys[row][col]);
        Serial.println(" pressed");
        digitalWrite(BLUE_LED, LOW);
        delay(500);
        digitalWrite(BLUE_LED, HIGH);
        digitalWrite(rowPins[row], LOW);
        return keys[row][col];
      }
    }
    digitalWrite(rowPins[row], LOW);
  }

  return 'n';
}

void loop() {
  char key = getKey();

  if (key != 'n'){
    if (key == '*'){
      input[0] = '*';
      input[1] = '*';
      input[2] = '*';
      input[3] = '*';
      input_cnt = 0;
    } else if (key == '#'){
      changeWifiOrRemoveLock();
    } else {
      input[input_cnt] = key; 
      input_cnt++; 
    }
  }

  if (input_cnt == 4){
    Serial.println("4 pins recived");
    Serial.print(input[0]);
    Serial.print(", ");
    Serial.print(input[1]);
    Serial.print(", ");
    Serial.print(input[2]);
    Serial.print(", ");
    Serial.println(input[3]);
    if (checkPass()){
      digitalWrite(RED_LED, HIGH);
      digitalWrite(BLUE_LED, HIGH);
      sendLog(1);
      delay(1000);
      digitalWrite(RED_LED, LOW);
      digitalWrite(BLUE_LED, LOW);
      sendLog(0);
    } else {
      sendLog(3);
      for (int tmp = 0; tmp < 10; tmp++){
        digitalWrite(BLUE_LED, HIGH);
        delay(500);
        digitalWrite(BLUE_LED, LOW);
        delay(500);
      }
    }
    input[0] = '*';
    input[1] = '*';
    input[2] = '*';
    input[3] = '*';
    input_cnt = 0;
  }
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

// Set the NodeMCU to first config mode
bool firstConfig() {
  WiFi.softAP(HOTSPOT_SSID);

  Serial.println("Access point mode enabled");
  Serial.print("SSID: ");
  Serial.println(HOTSPOT_SSID);

  IPAddress apIP = WiFi.softAPIP();
  Serial.print("Access point IP address: ");
  Serial.println(apIP);

  WiFiServer server(10221);
  server.begin();

  Serial.println("Listening on port 10221...");

  while (true) {
    WiFiClient client = server.available();

    if (client) {
      Serial.println("Client connected");
      String request = "";

      while (client.connected()) {
        if (client.available() > 0) {
          request += client.readStringUntil('\r');
        }
      }

      String data = request.substring(request.indexOf("{"));
      Serial.println(data);

      DynamicJsonDocument doc(1024);
      DeserializationError error = deserializeJson(doc, data);

      if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());

      } else {
        lockId = doc["LockId"].as<String>();
        userId = doc["UserId"].as<String>();
        wifiName = doc["WifiName"].as<String>();
        wifiPass = doc["WifiPass"].as<String>();        
      }

      // print extracted variables
      Serial.print("Lock ID: ");
      Serial.println(lockId);
      Serial.print("User ID: ");
      Serial.println(userId);
      Serial.print("Wi-Fi Name: ");
      Serial.println(wifiName);
      Serial.print("Wi-Fi Password: ");
      Serial.println(wifiPass);

      client.stop();
      Serial.println("Client disconnected");

      if (wifiName != ""){
        break;
      }
    }
  }

  // Save the variables to the EEPROM memory
  writeStringToEEPROM(LOCK_ID_ADDRESS, lockId);
  writeStringToEEPROM(USER_ID_ADDRESS, userId);
  writeStringToEEPROM(WIFI_NAME_ADDRESS, wifiName);
  writeStringToEEPROM(WIFI_PASS_ADDRESS, wifiPass);

  // Turn off access point mode
  WiFi.softAPdisconnect(true);

  Serial.println("Access point mode disabled");

  // Connect to the WiFi network using the saved credentials
  Serial.print("Connecting to ");
  Serial.println(wifiName);
  WiFi.begin(wifiName, wifiPass);

  int tries = 0;

  while (WiFi.status() != WL_CONNECTED && tries < 15) {
    delay(1000);
    Serial.print(".");
    tries++;
  }

  if (WiFi.status() != WL_CONNECTED){
    Serial.println("");
    Serial.println("WiFi connection error!");    
    return false;
  }

  Serial.println("");
  Serial.println("WiFi connected");

  // Send a request to the API with the lockId and config_successful data
  HTTPClient http;
  String apiMethod = API_URL + "first_config_l2";
  http.begin(wifi_client, apiMethod);
  http.addHeader("Content-Type", "application/json");
  String requestBody = "{\"LockId\":\"" + lockId + "\",\"UserId\":" + userId + "}";
  int httpCode = http.POST(requestBody);

  if (httpCode > 0) {
    String response = http.getString();
    Serial.println(response);

    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, response);
    http.end();

    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());

    } else {
      int status_rc = doc["status"];
      String lockId_rc = doc["lockId"].as<String>();
      String userId_rc = doc["userId"].as<String>();

      Serial.print(F("Status: "));
      Serial.println(status_rc);
      Serial.print(F("Lock ID: "));
      Serial.println(lockId);
      Serial.print(F("User ID: "));
      Serial.println(userId);

      if (status_rc == 1 && lockId_rc == lockId && userId_rc == userId) {
        Serial.println("Configurations completed");
        digitalWrite(BLUE_LED, HIGH);
        delay(1000);
        digitalWrite(BLUE_LED, LOW);
      } else {
        lockId = "null";
        userId = "null";
        wifiName = "null";
        wifiPass = "null";
        Serial.println("Configuration error, Configure again");
        for (int tmp = 0; tmp < 3; tmp++) {
          delay(500);
          digitalWrite(BLUE_LED, HIGH);
          delay(500);
          digitalWrite(BLUE_LED, LOW);
        }
        return false;
      }
    }
  }
  return true;
}

void sendLog(int logStatus){
  // status_labels = ['Locked', 'Unlocked', 'Successful attempt', 'Unsuccessful attempt', 'Error', 'First config', 'Wi-Fi change', 'Owner change']
  HTTPClient http;
  String apiMethod = API_URL + "proc_status";

  http.begin(wifi_client, apiMethod);

  // Testing purpose Only
  lockId = "3";
  userId = "3";
  // Testing done

  http.addHeader("Content-Type", "application/json");
  String requestBody = "{\"LockId\":\"" + lockId + "\",\"UserId\":" + userId + ",\"Status\":" + logStatus + "}";

  int httpCode = http.POST(requestBody);

  if (httpCode > 0) {
    String response = http.getString();

    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, response);
    http.end();

    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());

    } else {
      int status_rc = doc["Status"];

      Serial.print(F("Status: "));
      Serial.println(status_rc);
      
      if (status_rc == 1) {
        Serial.println("Log uploaded!");
        digitalWrite(BLUE_LED, HIGH);
        delay(1000);
        digitalWrite(BLUE_LED, LOW);

        return;

      } else {
        Serial.println("Error in log uploading");
        for (int tmp = 0; tmp < 3; tmp++) {
          delay(500);
          digitalWrite(BLUE_LED, HIGH);
          delay(500);
          digitalWrite(BLUE_LED, LOW);
        }
        return;
      }
    }
  }
  return;
}

bool checkPass(){
  HTTPClient http;
  String apiMethod = API_URL + "unlock_lock";
  
  http.begin(wifi_client, apiMethod);

  http.addHeader("Content-Type", "application/json");
  // Testing purpose Only
  lockId = "3";
  userId = "3";
  // Testing done
  String requestBody = "{\"LockId\":\"" + lockId + "\",\"UserId\":" + userId + "}";

  int httpCode = http.POST(requestBody);

  if (httpCode > 0) {
    String response = http.getString();

    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, response);
    http.end();

    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      sendLog(4);

    } else {
      int status_rc = doc["Status"];
      String pin_rc = doc["Pin"].as<String>();
      String userId_rc = doc["UserId"].as<String>();

      Serial.print(F("Status: "));
      Serial.println(status_rc);
      Serial.print(F("Pin: "));
      Serial.println(pin_rc);
      Serial.print(F("User ID: "));
      Serial.println(userId_rc);

      if (status_rc == 1 && userId_rc == userId) {
        Serial.println("Pin recived successfully");
        digitalWrite(BLUE_LED, HIGH);
        delay(1000);
        digitalWrite(BLUE_LED, LOW);

        bool pinCheck = true;
        for (int tmp = 0; tmp < 4; tmp++){
          if (pin_rc[tmp] != input[tmp]){
            pinCheck = false;
            break;
          }
        }

        return pinCheck;

      } else {
        Serial.println("Error in unlock process");
        sendLog(4);
        for (int tmp = 0; tmp < 3; tmp++) {
          delay(500);
          digitalWrite(BLUE_LED, HIGH);
          delay(500);
          digitalWrite(BLUE_LED, LOW);
        }
        return false;
      }
    }
  }
  return false;
}

int checkPassCR(){
  HTTPClient http;
  String apiMethod = API_URL + "cr_lock";
  
  http.begin(wifi_client, apiMethod);

  http.addHeader("Content-Type", "application/json");
  String requestBody = "{\"LockId\":\"" + lockId + "\",\"UserId\":" + userId + "}";

  int httpCode = http.POST(requestBody);

  if (httpCode > 0) {
    String response = http.getString();

    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, response);
    http.end();

    if (error) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      sendLog(4);

    } else {
      int status_rc = doc["Status"];
      String pin_rc = doc["Pin"].as<String>();
      String userId_rc = doc["UserId"].as<String>();
      int state_rc = doc["State"]; // states: false = 0, wifi change = 1, remove lock = 2

      Serial.print(F("Status: "));
      Serial.println(status_rc);
      Serial.print(F("Pin: "));
      Serial.println(pin_rc);
      Serial.print(F("User ID: "));
      Serial.println(userId_rc);
      Serial.print("State: ");
      Serial.println(state_rc);

      if (status_rc == 1 && userId_rc == userId) {
        Serial.println("Pin recived successfully");
        digitalWrite(BLUE_LED, HIGH);
        delay(1000);
        digitalWrite(BLUE_LED, LOW);

        bool pinCheck = true;
        for (int tmp = 0; tmp < 4; tmp++){
          if (pin_rc[tmp] != input[tmp]){
            pinCheck = false;
            break;
          }
        }

        if (pinCheck){
          return state_rc;
        } else {
          return 0;
        }        

      } else {
        Serial.println("Error in unlock process");
        sendLog(4);
        for (int tmp = 0; tmp < 3; tmp++) {
          delay(500);
          digitalWrite(BLUE_LED, HIGH);
          delay(500);
          digitalWrite(BLUE_LED, LOW);
        }
        return 0;
      }
    }
  }
  return 0;
}

bool changeWifiOrRemoveLock(){
  input[0] = '*';
  input[1] = '*';
  input[2] = '*';
  input[3] = '*';
  input_cnt = 0;
  for (int tmp = 0; tmp < 5; tmp++) {
    delay(500);
    digitalWrite(BLUE_LED, HIGH);
    delay(500);
    digitalWrite(BLUE_LED, LOW);
  }

  while (true){
    char key = getKey();

    if (key != 'n'){
      if (key == '*'){
        input[0] = '*';
        input[1] = '*';
        input[2] = '*';
        input[3] = '*';
        input_cnt = 0;
      } else if (key == '#'){
        continue;
      } else {
        input[input_cnt] = key; 
        input_cnt++; 
        Serial.println(key);
      }
    }

    if (input_cnt == 4){
      int state = checkPassCR();
      if (state == 1){
        // change Wifi
        if (changeWifi){
          sendLog(6);
          return true;
        } else {
          sendLog(3);
          return false;
        }
      } else if (state == 2) {
        // remove lock
        removeLock();
        sendLog(7);
        return true;
      } else {
        sendLog(3);
        input[0] = '*';
        input[1] = '*';
        input[2] = '*';
        input[3] = '*';
        input_cnt = 0;
        return false;
      }
    }
  }
  return false;
}

// Set the NodeMCU to first config mode
bool changeWifi() {
  WiFi.softAP(HOTSPOT_SSID);

  Serial.println("Access point mode enabled");
  Serial.print("SSID: ");
  Serial.println(HOTSPOT_SSID);

  String lockId_rc = "null";
  String userId_rc = "null";
  String wifiName_rc = "null";
  String wifiPass_rc = "null";

  IPAddress apIP = WiFi.softAPIP();
  Serial.print("Access point IP address: ");
  Serial.println(apIP);

  WiFiServer server(10221);
  server.begin();

  Serial.println("Listening on port 10221...");

  while (true) {
    WiFiClient client = server.available();

    if (client) {
      Serial.println("Client connected");
      String request = "";

      while (client.connected()) {
        if (client.available() > 0) {
          request += client.readStringUntil('\r');
        }
      }

      String data = request.substring(request.indexOf("{"));
      Serial.println(data);

      DynamicJsonDocument doc(1024);
      DeserializationError error = deserializeJson(doc, data);

      if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());

      } else {
        lockId_rc = doc["LockId"].as<String>();
        userId_rc = doc["UserId"].as<String>();
        wifiName_rc = doc["WifiName"].as<String>();
        wifiPass_rc = doc["WifiPass"].as<String>();        
      }

      // print extracted variables
      Serial.print("Lock ID Recived: ");
      Serial.println(lockId_rc);
      Serial.print("User ID Recived: ");
      Serial.println(userId_rc);
      Serial.print("Wi-Fi Name Recived: ");
      Serial.println(wifiName_rc);
      Serial.print("Wi-Fi Password Recived: ");
      Serial.println(wifiPass_rc);

      client.stop();
      Serial.println("Client disconnected");

      if (wifiName_rc != ""){
        break;
      }
    }
  }

  if (lockId != lockId_rc || userId != userId_rc){
    Serial.println("LockId or UserId does not match!");
    return false;
  }

  // Save the variables to the EEPROM memory
  wifiName = wifiName_rc;
  wifiPass = wifiPass_rc;
  writeStringToEEPROM(WIFI_NAME_ADDRESS, wifiName);
  writeStringToEEPROM(WIFI_PASS_ADDRESS, wifiPass);

  // Turn off access point mode
  WiFi.softAPdisconnect(true);

  Serial.println("Access point mode disabled");

  // Connect to the WiFi network using the saved credentials
  Serial.print("Connecting to ");
  Serial.println(wifiName);
  WiFi.begin(wifiName, wifiPass);

  int tries = 0;

  while (WiFi.status() != WL_CONNECTED && tries < 15) {
    delay(1000);
    Serial.print(".");
    tries++;
  }

  if (WiFi.status() != WL_CONNECTED){
    Serial.println("");
    Serial.println("WiFi connection error!");    
    return false;
  }

  Serial.println("");
  Serial.println("WiFi connected");

  return true;
}

void removeLock(){
  lockId = "null";
  userId = "null";
  wifiName = "null";
  wifiPass = "null";


  // Save the variables to the EEPROM memory
  writeStringToEEPROM(LOCK_ID_ADDRESS, lockId);
  writeStringToEEPROM(USER_ID_ADDRESS, userId);
  writeStringToEEPROM(WIFI_NAME_ADDRESS, wifiName);
  writeStringToEEPROM(WIFI_PASS_ADDRESS, wifiPass);

  ESP.restart();
}
