#include <Arduino.h>
#include "dice.h"

#include "freertos/task.h"

DiceSequence::DiceSequence(TaskHandle_t task) : Sequence(task) {
  maximum = 6;
  target = 3;
}

void DiceSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "DiceTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void DiceSequence::TaskCode (void *pparams) {
  int n;
  DiceSequence *pthis = (DiceSequence*) pparams;

  for (n = 1; n <= 6; n++) {
    Serial.printf("Dice on %d\n", n);
    vTaskDelay(300);
  }

  Serial.printf("Dice settled on %d\n", pthis->target);
  pthis->complete();
  vTaskDelete(NULL);
}