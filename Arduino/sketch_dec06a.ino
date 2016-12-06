#include <SoftwareSerial.h>
SoftwareSerial BTSerial(3,2);


int trigPin = 4;
int echoPin = 5;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  BTSerial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  float duration, distance;
  digitalWrite(trigPin, HIGH);
  delay(10);
  digitalWrite(trigPin, LOW);
 

  duration = pulseIn(echoPin, HIGH);

  distance = ((float)(340 * duration) / 10000) / 2;
  delay(1000);
  if(BTSerial.available()){
    Serial.write(BTSerial.read());
  }
  if(distance <30)
  {
    Serial.println(distance);
      char data='a';
     BTSerial.write(data);
  
  
  }
  
 

}
