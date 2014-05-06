// boblightd receiver for WS_2811, boblightd device type=momo, prefix 55 AA
// capable at least for 20Hz input stream if LED count <= 150
// by TPertenava
// last modified 22.10.13

#include "FastLED.h"

#define NUM_LEDS 5 // LED count
#define CHANNELS NUM_LEDS*3 // each output for R, G and B
#define LED_PIN 3 // arduino output pin
#define BRIGHTNESS 96 // maximum brightness
#define SPEED 38400 // virtual serial port speed, must be the same in boblight_config 

CRGB leds[NUM_LEDS];

void setup()
{
  delay(2000);
  randomSeed(0x513);
  LEDS.setBrightness(BRIGHTNESS);
  LEDS.addLeds<WS2811, LED_PIN, BRG>(leds, NUM_LEDS);
}

byte values[NUM_LEDS][3]; // 2-level array, 1 level is for led number, 2 level is for rgb values

void loop() { 

  int lastR = 0xA1;
  int lastG = 0x19;
  int lastB = 0x88;
  
  byte reversR = 1;
  byte reversG = 0;
  byte reversB = 1;
  
  int rndColor = 0;
  int rndDiv = 0;
  
  while(1){

    if(reversR == 0){
      lastR += random(3);
    }else{
      lastR -= random(3);
    }
    
    if(lastR >= 0xFB){
      reversR = 1;
    }
    if(lastR <= 0x3){
      reversR = 0;
    }
    
    if(reversG == 0){
      lastG += random(3);
    }else{
      lastG -= random(3);
    }
    
   if(lastG >= 0xFB){
      reversG = 1;
    }
    if(lastG <= 0x3){
      reversG = 0;
    }
    
    if(reversB == 0){
      lastB += random(3);
    }else{
      lastB -= random(3);
    }
    
    if(lastB >= 0xFB){
      reversB = 1;
    }
    if(lastB <= 0x3){
      reversB = 0;
    }    
   
    for (byte Led = 0; Led<NUM_LEDS; Led++) {
      values[Led][0] = lastR;
      values[Led][1] = lastG;
      values[Led][2] = lastB;
    } 
    for (byte Led = 0; Led < NUM_LEDS; Led++)
    {
      
      byte red = values[Led][0];
      byte green = values[Led][1];
      byte blue = values[Led][2];
      leds[Led] = CRGB(blue, red, green);
     }
     LEDS.show();
      delay(5);
  }
   
  
  //memset(leds, 0,  NUM_LEDS * sizeof(struct CRGB)); //filling Led array by zeroes
  
  /*LEDS.show();
  delay(1000);

  for (byte Led = 0; Led < NUM_LEDS; Led++)
  {
    
    byte red = values[Led][0];
    byte green = values[Led][1];
    byte blue = values[Led][2];
    leds[Led] = CRGB(blue, red, green);
   } 
    
  LEDS.show();
  
  delay(1000);*/
}
