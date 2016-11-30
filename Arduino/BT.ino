#include <SoftwareSerial.h>
int blueTx=3;   //Tx (보내는핀 설정)at
int blueRx=2;   //Rx (받는핀 설정)
uint8_t buffer[1024];//데이터를 수신 받는 버퍼
uint8_t bufferPosition;
SoftwareSerial mySerial(blueTx, blueRx);
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);   //시리얼모니터
  mySerial.begin(9600); //블루투스 시리얼
  bufferPosition = 0; //버퍼 위치 초기화

}

void loop() {
  // put your main code here, to run repeatedly:
    if (mySerial.available()) {
      uint8_t data = mySerial.read();       
      Serial.write(data);  //블루투스측 내용을 시리얼모니터에 출력
      buffer[bufferPosition++]=data;
      if (data=='\n') {  //문자열 종료 표시
        buffer[bufferPosition] = '\0';          
        mySerial.write(buffer,bufferPosition);
        
      }
      bufferPosition=0;
    }
}
