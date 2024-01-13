#include "timer.h"

#include "../output.h"
#include "freertos/task.h"

TimerSequence::TimerSequence(TaskHandle_t task, int duration) : Sequence(task) {
  this->duration = duration;
}

void TimerSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "TimerTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void TimerSequence::TaskCode(void *pparams) {
  TimerSequence *pthis = (TimerSequence *)pparams;
  char s[16];
  int n;

  pthis->current = pthis->duration;

  while (1) {
    sprintf(s, "%d", pthis->current);
    if (pthis->current <= 9) {
      Output.setFont(NORMAL);
      Output.drawCentre(pthis->current + '0');
    } else {
      Output.setFont(SMALL);
      Output.drawText(s);
    }
    if (pthis->current == 0) {
      break;
    }
    pthis->current--;
    vTaskDelay(1000);
  }

  for (n = 0; n < 8; n++) {
    vTaskDelay(200);
    Output.blank();
    vTaskDelay(200);
    Output.restore();
  }

  pthis->complete();
  vTaskDelete(NULL);
}