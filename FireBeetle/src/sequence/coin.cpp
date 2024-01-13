#include "coin.h"

#include "../output.h"
#include "esp_random.h"
#include "freertos/task.h"

CoinSequence::CoinSequence(TaskHandle_t task) : Sequence(task) {
  heads = (esp_random() % 2) == 1;
}

void CoinSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "CoinTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void CoinSequence::TaskCode(void *pparams) {
  int n;
  CoinSequence *pthis = (CoinSequence *)pparams;

  Output.setFont(DICE);

  for (n = 0; n < 5; n++) {
    Serial.println("Heads");
    Output.drawCentre('H');
    vTaskDelay(80);
    Serial.println("Tails");
    Output.drawCentre('T');
    vTaskDelay(80);
  }

  if (pthis->heads) {
    Output.drawCentre('H');
  } else {
    Output.drawCentre('T');
  }

  pthis->complete();
  vTaskDelete(NULL);
}