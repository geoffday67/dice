#include "timer.h"

#include "freertos/task.h"

TimerSequence::TimerSequence(TaskHandle_t task, int duration) : Sequence(task) {
  this->duration = duration;
}

void TimerSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "TimerTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void TimerSequence::TaskCode(void *pparams) {
  TimerSequence *pthis = (TimerSequence *)pparams;

  pthis->current = pthis->duration;
  while (1) {
    Serial.printf("Current timer = %d\n", pthis->current);
    if (pthis->current == 0) {
      break;
    }
    pthis->current--;
    vTaskDelay(1000);
  }
  pthis->complete();
  vTaskDelete(NULL);
}