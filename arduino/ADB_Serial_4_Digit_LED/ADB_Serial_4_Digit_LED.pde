#include <SPI.h>
#include <Adb.h>
#include <SoftwareSerial.h>

Connection * connection;

// Pins for TTL 4 digit serial display
#define rxPin 2
#define txPin 3

SoftwareSerial softSerial =  SoftwareSerial(rxPin, txPin);

// Event handler for shell connection; called whenever data sent from Android to Microcontroller
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  // Data packets contain four bytes: one for each digit of LED
  if (event == ADB_CONNECTION_RECEIVE)
  {
    // Output debugging to serial
    Serial.println("Data Received!"); 
    Serial.println(data[0],DEC);
    Serial.println(data[1],DEC);
    Serial.println(data[2],DEC); 
    Serial.println(data[3],DEC);  
    
    // Get digit values from byte array
    int thousands = data[0];
    int hundreds = data[1];
    int tens = data[2];
    int ones = data[3];

    // Convert into actual value
    int value = thousands * 1000;
    value += (hundreds * 100);
    value += (tens * 10);
    value += ones;
    
    displaySeg(value); // Output result to LED display
    Serial.println(value,DEC); // Output debugging to serial
  }
}

void setup()
{
  // Init serial port for debugging
  Serial.begin(57600);
  
  // Define pin modes for tx, rx
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  
  // Set the data rate for the SoftwareSerial port
  softSerial.begin(9600);
  //v=0x76. To reset display module
  softSerial.print("vv");
  
  // Init the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect. Use port number 4568
  connection = ADB::addConnection("tcp:4567", true, adbEventHandler);  
}

void loop()
{
  // Poll the ADB subsystem.
  ADB::poll();
}

void displaySeg(int mg) {

  if(mg < 10) {
    softSerial.print("   ");
    softSerial.print(mg);
  } else if(mg < 100) {
    softSerial.print("  ");
    softSerial.print(mg);
  } else if(mg < 1000) {
    softSerial.print(" ");
    softSerial.print(mg);
  } else if(mg < 1000) {
    softSerial.print(" ");
    softSerial.print(mg);
  } else {
    softSerial.print(mg);
  }
}

