#include "coin.h"

#include "freertos/task.h"

CoinSequence::CoinSequence(TaskHandle_t task) : Sequence(task) {
  heads = true;
}

void CoinSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "CoinTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void CoinSequence::TaskCode(void *pparams) {
  int n;
  CoinSequence *pthis = (CoinSequence *)pparams;

  for (n = 0; n < 5; n++) {
    Serial.println("Heads");
    vTaskDelay(200);
    Serial.println("Tails");
    vTaskDelay(200);
  }

  if (pthis->heads) {
    Serial.println("Finished on heads");
  } else {
    Serial.println("Finished on tails");
  }

  pthis->complete();
  vTaskDelete(NULL);
}