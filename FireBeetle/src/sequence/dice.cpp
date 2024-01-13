#include "dice.h"

#include "../output.h"
#include "esp_random.h"
#include "freertos/task.h"

DiceSequence::DiceSequence(TaskHandle_t task) : Sequence(task) {
  retainDisplay = true;
  target = (esp_random() % 6) + 1;
}

void DiceSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "DiceTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void DiceSequence::TaskCode(void *pparams) {
  int n, m;
  DiceSequence *pthis = (DiceSequence *)pparams;

  Output.setFont(DICE);

  for (m = 1; m <= 2; m++) {
    for (n = 1; n <= 6; n++) {
      pthis->showNumber(n);
      vTaskDelay(80);
    }
  }

  pthis->showNumber(pthis->target);
  pthis->complete();
  vTaskDelete(NULL);
}

void DiceSequence::showNumber(int number) {
  Output.drawCentre(number + '0');
}