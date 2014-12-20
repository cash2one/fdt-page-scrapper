#include <Adafruit_NeoPixel.h>

#define PIN 3
#define LENGHT 16
#define HEIGHT 12

#define SHOW_MATRIX_LENGHT 12
#define SHOW_MATRIX_HEIGHT 8

struct stripPixel
{
  uint8_t x;
  uint8_t y;
};

struct alfabetEntry
{
  char strValue;
  uint8_t lenght;
  uint8_t* alfabetMatrix;
};

uint8_t symbolRusSpace[2] = {0,0};

//А
uint8_t symbolRusA[5] = {127,136,136,136,127};
//Б                        
uint8_t symbolRusB[5] = {255,145,145,145,14};
//В                        
uint8_t symbolRusV[5] = {255,145,145,145,110};			
//Г                        
uint8_t symbolRusG[5] = {255,128,128,128,128};			
//Д			
uint8_t symbolRusD[6] = {3,254,130,130,254,3};			
//Е		       
uint8_t symbolRusE[5] = {255,145,145,145,129};			
//Ё		       
uint8_t symbolRusEO[5] = {127,201,73,201,65};			
//Ж
uint8_t symbolRusJ[7] = {199,40,16,255,16,40,199};
//З
uint8_t symbolRusZ[5] = {129,145,145,145,110};
//И
uint8_t symbolRusI[5] = {255,8,16,32,255};
//Й
uint8_t symbolRusII[5] = {255,8,144,32,255};
//К
uint8_t symbolRusK[5] = {255,16,16,40,199};
//Л
uint8_t symbolRusL[5] = {63,64,128,128,255};
//М
uint8_t symbolRusM[7] = {255,64,32,16,32,64,255};
//Н
uint8_t symbolRusN[5] = {255,16,16,16,255};
//О
uint8_t symbolRusO[5] = {126,129,129,129,126};
//П
uint8_t symbolRusP[5] = {255,128,128,128,255};
//Р
uint8_t symbolRusR[5] = {255,136,136,136,112};
//С
uint8_t symbolRusS[5] = {126,129,129,129,66};
//Т
uint8_t symbolRusT[5] = {128,128,255,128,128};
//У
uint8_t symbolRusY[5] = {240,9,9,9,254};
//Ф
uint8_t symbolRusF[7] = {112,136,136,255,136,136,112};
//Х
uint8_t symbolRusX[5] = {199,40,16,40,199};
//Ц
uint8_t symbolRusCC[6] = {254,2,2,2,254,3};
//Ч
uint8_t symbolRusCh[5] = {240,8,8,8,255};
//Ш
uint8_t symbolRusSh[5] = {255,1,255,1,255};
//Щ
uint8_t symbolRusSCH[6] = {254,2,254,2,254,3};
//Ъ
uint8_t symbolRusHard[6] = {128,255,17,17,17,14};
//Ы
uint8_t symbolRusYY[7] = {255,17,17,17,14,0,255};
//Ь
uint8_t symbolRusSoft[5] = {255,17,17,17,14};
//Э
uint8_t symbolRusEE[5] = {66,129,145,145,126};
//Ю
uint8_t symbolRusU[6] = {255,16,126,129,129,126};
//Я
uint8_t symbolRusYa[5] = {99,148,152,144,127};
//Сердце - Полное
uint8_t symbolRusHeartFull[9] = {48,120,204,102,51,102,204,120,48};
//Сердце - Big
uint8_t symbolRusHeartBig[9] = {48,72,132,66,33,66,132,72,48};
//Сердце - Small
uint8_t symbolRusHeartSmall[7] = {48,72,36,18,36,72,48};



struct alfabetEntry entrySpace = {' ',2,(uint8_t*)symbolRusSpace};		     
struct alfabetEntry entryA = {'\u0410',5,(uint8_t*)symbolRusA};//А
struct alfabetEntry entryB = {'\u0411',5,(uint8_t*)symbolRusB};//Б
struct alfabetEntry entryV = {'\u0412',5,(uint8_t*)symbolRusV};//В			
struct alfabetEntry entryG = {'\u0413',5,(uint8_t*)symbolRusG};//Г
struct alfabetEntry entryD = {'\u0414',6,(uint8_t*)symbolRusD};//Д			
struct alfabetEntry entryE = {'\u0415',5,(uint8_t*)symbolRusE};//Е			
struct alfabetEntry entryEO = {'\u0401',5,(uint8_t*)symbolRusEO};//Ё			
struct alfabetEntry entryJ = {'\u0416',7,(uint8_t*)symbolRusJ};//Ж
struct alfabetEntry entryZ = {'\u0417',5,(uint8_t*)symbolRusZ};//З
struct alfabetEntry entryI = {'\u0418',5,(uint8_t*)symbolRusI};//И
struct alfabetEntry entryII = {'\u0419',5,(uint8_t*)symbolRusII};//Й
struct alfabetEntry entryK = {'\u041a',5,(uint8_t*)symbolRusK};//К
struct alfabetEntry entryL = {'\u041b',5,(uint8_t*)symbolRusL};//Л
struct alfabetEntry entryM = {'\u041c',7,(uint8_t*)symbolRusM};//М 
struct alfabetEntry entryN = {'\u041d',5,(uint8_t*)symbolRusN};//Н
struct alfabetEntry entryO = {'\u041e',5,(uint8_t*)symbolRusO};//О
struct alfabetEntry entryP = {'\u041f',5,(uint8_t*)symbolRusP};//П
struct alfabetEntry entryR = {'\u0420',5,(uint8_t*)symbolRusR};//Р
struct alfabetEntry entryS = {'\u0421',5,(uint8_t*)symbolRusS};//С
struct alfabetEntry entryT = {'\u0422',5,(uint8_t*)symbolRusT};//Т
struct alfabetEntry entryY = {'\u0423',5,(uint8_t*)symbolRusY};//У
struct alfabetEntry entryF = {'\u0424',7,(uint8_t*)symbolRusF};//Ф
struct alfabetEntry entryX = {'\u0425',5,(uint8_t*)symbolRusX};//Х 
struct alfabetEntry entryCC = {'\u0426',6,(uint8_t*)symbolRusCC};//Ц
struct alfabetEntry entryCh = {'\u0427',5,(uint8_t*)symbolRusCh};//Ч
struct alfabetEntry entrySh = {'\u0428',5,(uint8_t*)symbolRusSh};//Ш
struct alfabetEntry entrySCH = {'\u0429',6,(uint8_t*)symbolRusSCH};//Щ
struct alfabetEntry entryHard = {'\u042a',6,(uint8_t*)symbolRusHard};//Ъ 
struct alfabetEntry entryYY = {'\u042b',7,(uint8_t*)symbolRusYY};//Ы
struct alfabetEntry entrySoft = {'\u042c',5,(uint8_t*)symbolRusSoft};//Ь
struct alfabetEntry entryEE = {'\u042d',5,(uint8_t*)symbolRusEE};//Э 
struct alfabetEntry entryU = {'\u042e',6,(uint8_t*)symbolRusU};//Ю
struct alfabetEntry entryYa = {'\u042f',5,(uint8_t*)symbolRusYa};//Я

struct alfabetEntry entryHeartFull = {'@',9,(uint8_t*)symbolRusHeartFull};//full heart
struct alfabetEntry entryHeartBig = {'#',9,(uint8_t*)symbolRusHeartBig};//big heart
struct alfabetEntry entryHeartSmall = {'!',7,(uint8_t*)symbolRusHeartSmall};//small heart

alfabetEntry alfabetList[] = {entrySpace, entryA, entryB, entryV, entryG, entryD, 
			      entryE, entryEO, entryJ, entryZ, entryI, entryII, 
			      entryK, entryL, entryM, entryN, entryO, entryP, 
			      entryR, entryS, entryT, entryY, entryF, entryX, 
			      entryCC, entryCh, entrySh, entrySCH, entryHard, 
			      entryYY, entrySoft, entryEE, entryU, entryYa,
                              entryHeartFull, entryHeartBig, entryHeartSmall};
			      
			      
uint8_t alfabetListLenght = 37;

//ВЕРОНИКА 
char workString[] = "\u0412\u0415\u0420\u041e\u041d\u0418\u041a\u0410 \u0412\u042b\u0425\u041e\u0414\u0418 \u0417\u0410 \u041c\u0415\u041d\u042f \u0417\u0410\u041c\u0423\u0416";
int curCharIndex = 0;
int curCharPartIndex = 0;
int stringSize = 60;

//disappearance block
int curSymbolBrightness = 0;
int increasingBrightness = 0;
int brightnessStepSize = 6;

//флаг мигания сердечка
int heartFlashNeed = 0;
int heartFlashCount = 0;

// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
// NEO_KHZ800 800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
// NEO_KHZ400 400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
// NEO_GRB Pixels are wired for GRB bitstream (most NeoPixel products)
// NEO_RGB Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(LENGHT * HEIGHT, PIN, NEO_GRB + NEO_KHZ800);

//area for data output

// stripPixel tempArray[] = {{1,0},{2,0},{3,0},{0,1},{4,1},{0,2},{4,2},{0,3},{4,3},{0,4},{1,4},{2,4},{3,4},{4,4},{0,5},{4,5},{0,6},{4,6},{0,7},{4,7}};

uint8_t showMatrix[SHOW_MATRIX_HEIGHT][SHOW_MATRIX_LENGHT];

uint8_t shwMtrxPnrt = 0;

stripPixel frameCoordinate[52] = {{0,0},{1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0},{8,0},{9,0},{10,0},{11,0},{12,0},{13,0},{14,0},{15,0},{15,1},{15,2},{15,3},{15,4},{15,5},{15,6},{15,7},{15,8},{15,9},{15,10},{15,11},{14,11},{13,11},{12,11},{11,11},{10,11},{9,11},{8,11},{7,11},{6,11},{5,11},{4,11},{3,11},{2,11},{1,11},{0,11},{0,10},{0,9},{0,8},{0,7},{0,6},{0,5},{0,4},{0,3},{0,2},{0,1}};

uint8_t frameMapping[12][16] = {{0,	1,	2,	3,	4,	5,	6,	7,	8,	9,	10,	11,	12,	13,	14,	15},
			  {31,	30,	29,	28,	27,	26,	25,	24,	23,	22,	21,	20,	19,	18,	17,	16},
			  {32,	33,	34,	35,	36,	37,	38,	39,	40,	41,	42,	43,	44,	45,	46,	47},
			  {63,	62,	61,	60,	59,	58,	57,	56,	55,	54,	53,	52,	51,	50,	49,	48},
			  {64,	65,	66,	67,	68,	69,	70,	71,	72,	73,	74,	75,	76,	77,	78,	79},
			  {95,	94,	93,	92,	91,	90,	89,	88,	87,	86,	85,	84,	83,	82,	81,	80},
			  {96,	97,	98,	99,	100,	101,	102,	103,	104,	105,	106,	107,	108,	109,	110,	111},
			  {127,	126,	125,	124,	123,	122,	121,	120,	119,	118,	117,	116,	115,	114,	113,	112},
			  {128,	129,	130,	131,	132,	133,	134,	135,	136,	137,	138,	139,	140,	141,	142,	143},
			  {159,	158,	157,	156,	155,	154,	153,	152,	151,	150,	149,	148,	147,	146,	145,	144},
			  {160,	161,	162,	163,	164,	165,	166,	167,	168,	169,	170,	171,	172,	173,	174,	175},
			  {191,	190,	189,	188,	187,	186,	185,	184,	183,	182,	181,	180,	179,	178,	177,	176}};
			  


// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel. Avoid connecting
// on a live circuit...if you must, connect GND first.

void setup() {
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
  //zeroing showMatrix
  //memset(showMatrix, 0, sizeof(int)*SHOW_MATRIX_HEIGHT*SHOW_MATRIX_LENGHT);
  zeroingShwMtrx();
}

void loop() {
  // Some example procedures showing how to display to the pixels:
  //colorWipe(strip.Color(255, 255, 255), 500); // White
  //colorWipe(strip.Color(255, 0, 0), 500); // Red
  //colorWipe(strip.Color(0, 0, 0), 1000); // White
  //colorWipe(strip.Color(0, 255, 0), 50); // Green
  //colorWipe(strip.Color(0, 0, 255), 50); // Blue
  // Send a theater pixel chase in...
  //theaterChase(strip.Color(127, 127, 127), 50); // White
  //theaterChase(strip.Color(127, 0, 0), 50); // Red
  //theaterChase(strip.Color( 0, 0, 127), 50); // Blue

  //rainbow(10);
  //theaterChaseRainbow(50);
  frameCycle(1);
}

int getPixelNumber(uint8_t x, uint8_t y){
  if(x <= LENGHT && y <= HEIGHT){
    return frameMapping[y][x];
  }else{
    return LENGHT * HEIGHT;
  }
}

void printShowMatrix(uint8_t shiftX, uint8_t shiftY, uint32_t color){
  uint8_t i, j;
  for(i = 0; i < SHOW_MATRIX_LENGHT; i++){
      for(j = 0; j < SHOW_MATRIX_HEIGHT; j++){
	  if(showMatrix[j][(i + shwMtrxPnrt) % SHOW_MATRIX_LENGHT] > 0){
	    strip.setPixelColor(getPixelNumber(i + shiftX, j + shiftY), color);
	  }else{
	    strip.setPixelColor(getPixelNumber(i + shiftX, j + shiftY), strip.Color(0, 0, 0));
	  }
      }
  }
}

void zeroingShwMtrx(){
  uint8_t i,j;
  for(j = 0; j < SHOW_MATRIX_HEIGHT; j++){
    for(i = 0; i < SHOW_MATRIX_LENGHT; i++){
      showMatrix[j][i] = 0;
    }
  }
}
  
void nextRunnableStep(){
    uint8_t i,j;
    alfabetEntry wrkAE;
    char curChr = workString[curCharIndex];
    wrkAE = findAlfabetEntryByChar(curChr);
  
    if(curCharPartIndex == wrkAE.lenght){
      curCharIndex++;
      curChr = workString[curCharIndex];
      wrkAE = findAlfabetEntryByChar(curChr);
      curCharPartIndex = 0;
    }
    
    showMatrix[0][shwMtrxPnrt] = (128 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[1][shwMtrxPnrt] = (64 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[2][shwMtrxPnrt] = (32 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[3][shwMtrxPnrt] = (16 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[4][shwMtrxPnrt] = (8 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[5][shwMtrxPnrt] = (4 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[6][shwMtrxPnrt] = (2 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
    showMatrix[7][shwMtrxPnrt] = (1 & wrkAE.alfabetMatrix[curCharPartIndex])?1:0;
   
    curCharPartIndex++;
}

//буквы появляются и пропадают
void nextFlashingStep(){
    uint8_t i,j, redrawingNeed, shift;
    alfabetEntry wrkAE;
    char curChr = workString[curCharIndex];
    wrkAE = findAlfabetEntryByChar(curChr);
    redrawingNeed = 0;
  
    //если встретили первый символ строки, то начинаем увеличивть яркость, при этом обновляем вывод
    if(increasingBrightness == 0 && curSymbolBrightness == 0 ){
      increasingBrightness = 1;
      redrawingNeed=1;
      //иначе, если же оказалось что буква была уже показана, то ищем следующую букву
    }else if(curSymbolBrightness == 0){
       //getting new char from string and start new flashing process
       increasingBrightness = 1;
       curCharIndex++;
       curChr = workString[curCharIndex];
       wrkAE = findAlfabetEntryByChar(curChr);
       redrawingNeed = 1;
    }
      
    //если необходимо показывать сроку, а не сердце
    if(curCharIndex < stringSize && heartFlashNeed <= 0){
      //делаем что-нибудь
    }else{
      heartFlashNeed = 10;
      curCharIndex = 0;
    }
    
    //увеличиваем яроксть
    if(increasingBrightness == 1){
      curSymbolBrightness += brightnessStepSize;
    }else{
      curSymbolBrightness -= brightnessStepSize;
    }
    
    //если достигли максимальной яркости, то начинаем уменьшать яркость
    if(curSymbolBrightness >= 255){
      curSymbolBrightness = 255;
      increasingBrightness = 0;
    }else if(curSymbolBrightness <= 0){
      curSymbolBrightness = 0;
      increasingBrightness = 1;
    }
     
    if(heartFlashNeed <= 0)
    { 
      //вычисляем сдвиг для отображения букв по центру
      shift = SHOW_MATRIX_LENGHT/2 - wrkAE.lenght/2;
      
      if(redrawingNeed == 1){
        zeroingShwMtrx();
        for(i = 0; i < wrkAE.lenght; i++){
  	showMatrix[0][i + shift] = (128 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[1][i + shift] = (64 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[2][i + shift] = (32 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[3][i + shift] = (16 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[4][i + shift] = (8 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[5][i + shift] = (4 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[6][i + shift] = (2 & wrkAE.alfabetMatrix[i])?1:0;
  	showMatrix[7][i + shift] = (1 & wrkAE.alfabetMatrix[i])?1:0;
        }
      }
    }
}

void printCharToShowMatrix(struct alfabetEntry wrkAE){
  //вычисляем сдвиг для отображения букв по центру
  int i = 0;
  
  int shift = SHOW_MATRIX_LENGHT/2 - wrkAE.lenght/2;

  for(i = 0; i < wrkAE.lenght; i++){
    showMatrix[0][i + shift] = (128 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[1][i + shift] = (64 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[2][i + shift] = (32 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[3][i + shift] = (16 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[4][i + shift] = (8 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[5][i + shift] = (4 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[6][i + shift] = (2 & wrkAE.alfabetMatrix[i])?1:0;
    showMatrix[7][i + shift] = (1 & wrkAE.alfabetMatrix[i])?1:0;
  }
}

void printCharToShowMatrixWithColor(struct alfabetEntry wrkAE, uint32_t color){
  //вычисляем сдвиг для отображения букв по центру
  int i = 0;
  int shiftX = 2;
  int shiftY = 2;
  
  int shift = SHOW_MATRIX_LENGHT/2 - wrkAE.lenght/2;

  for(i = 0; i < wrkAE.lenght; i++){
    
    if((128 & wrkAE.alfabetMatrix[i]))
      strip.setPixelColor(getPixelNumber(i + shift + shiftX,0 + shiftY), color);

    if((64 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX, 1 + shiftY), color);
    }
    
    if((32 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,2 + shiftY), color);
    }
    
    if((16 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,3 + shiftY), color);
    }
    
    if((8 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,4 + shiftY), color);
    }
    
    if((4 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,5 + shiftY), color);
    }
    
    if((2 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,6 + shiftY), color);
    }
    
    if((1 & wrkAE.alfabetMatrix[i])){
      strip.setPixelColor(getPixelNumber(i + shift + shiftX ,7 + shiftY), color);
    }
    
  }
}

struct alfabetEntry findAlfabetEntryByChar(char chr){
  uint8_t i;
  for(i = 0; i < alfabetListLenght; i++){
    if(alfabetList[i].strValue == chr){
      return alfabetList[i];
    }
  }
  
  return alfabetList[0];
}

// Fill the dots one after the other with a color
void colorWipe(uint32_t c, uint8_t wait) {
  for(uint16_t i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, c);
      strip.show();
      delay(wait);
  }
}

void rainbow(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256 * 10; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel((i+j) & 255));
    }
    //strip.show();
    //delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void frameCycle(uint8_t wait) {
  uint16_t i, j, k;

  for(j=0; j<256*10; j++) { // 5 cycles of all colors on wheel
    // cleat matrix
    
    /*for (int i=0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, 0);
    }*/
    
    //печатаем буквы
    if(j % 1 == 0){     
	//flashing
	nextFlashingStep();
	
        if(heartFlashNeed){
            if(j % 1 == 0){
              zeroingShwMtrx();
              printCharToShowMatrixWithColor(entryHeartSmall, strip.Color(255, 0, 0));
              printCharToShowMatrixWithColor(entryHeartBig, strip.Color(curSymbolBrightness, 0, 0));
              delay(10);
            }	
        }else{
            printShowMatrix(2,2,strip.Color(curSymbolBrightness, 0, 0));
        }
    }
    
    if(j % 6 == 0){
	if(shwMtrxPnrt == SHOW_MATRIX_LENGHT){
	  shwMtrxPnrt = 0;
	}
	
	//Get new column runnable string
	//nextRunnableStep();
	//shwMtrxPnrt++;
	//printShowMatrix(2,2,strip.Color(255, 0, 0));
	
	strip.show();
    }
    
    //printAlfabet(alfabetA, 20, j % 17 - 4, 2, strip.Color(255, 0, 0));
  
    //печатаем сердечко маленькое
    
    
    //печатаем рамку
    for(i=0; i < 52; i++) {
      //strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
      strip.setPixelColor(getPixelNumber(frameCoordinate[i].x, frameCoordinate[i].y), Wheel(((i * 256 / 52) + j) & 255));
    }
	
    strip.show();
    delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void printAlfabet(struct stripPixel alfabetCrdnt[], int pixelCount, uint8_t shiftX, uint8_t shiftY, uint32_t color) {
  uint16_t i;
  for(i=0; i < pixelCount; i++) {
    strip.setPixelColor(getPixelNumber(alfabetCrdnt[i].x + shiftX, alfabetCrdnt[i].y + shiftY), color);
  }
}

//Theatre-style crawling lights.
void theaterChase(uint32_t c, uint8_t wait) {
  for (int j=0; j<10; j++) { //do 10 cycles of chasing
    for (int q=0; q < 3; q++) {
      for (int i=0; i < strip.numPixels(); i=i+3) {
	strip.setPixelColor(i+q, c); //turn every third pixel on
      }
      strip.show();
     
      delay(wait);
     
      for (int i=0; i < strip.numPixels(); i=i+3) {
	strip.setPixelColor(i+q, 0); //turn every third pixel off
      }
    }
  }
}

//Theatre-style crawling lights with rainbow effect
void theaterChaseRainbow(uint8_t wait) {
  for (int j=0; j < 256; j++) { // cycle all 256 colors in the wheel
    for (int q=0; q < 3; q++) {
	for (int i=0; i < strip.numPixels(); i=i+3) {
	  strip.setPixelColor(i+q, Wheel( (i+j) % 255)); //turn every third pixel on
	}
	strip.show();
       
	delay(wait);
       
	for (int i=0; i < strip.numPixels(); i=i+3) {
	  strip.setPixelColor(i+q, 0); //turn every third pixel off
	}
    }
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  if(WheelPos < 85) {
   return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if(WheelPos < 170) {
   WheelPos -= 85;
   return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170;
   return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}


