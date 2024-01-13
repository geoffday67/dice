#pragma once

#include <U8g2lib.h>

enum OutputFont {
  DICE, SMALL, NORMAL
};

class OutputClass {
 private:
  const int Clock = 15;
  const int ChipSelect = 21;
  const int DataIn = 22;

  U8G2_MAX7219_8X8_1_4W_SW_SPI *pDisplay;
  OutputFont font;

 public:
  OutputClass();

  void begin();
  void drawText(char*);
  void drawCentre(char);
  void flush();
  void clear();
  void blank();
  void restore();
  void setFont(OutputFont);
};

extern OutputClass Output;