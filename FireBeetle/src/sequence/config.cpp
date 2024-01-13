#include "config.h"

#include "../output.h"
#include "freertos/task.h"

ConfigSequence::ConfigSequence(TaskHandle_t task) : Sequence(task) {
  retainDisplay = false;
}

void ConfigSequence::start() {
  xTaskCreatePinnedToCore(TaskCode, "ConfigTask", 8192, this, 100, NULL, APP_CPU_NUM);
}

void ConfigSequence::TaskCode(void *pparams) {
  ConfigSequence *pthis = (ConfigSequence *)pparams;

  Output.setFont(DICE);
  Output.drawCentre('A');
  vTaskDelay(1000);
  pthis->complete();
  vTaskDelete(NULL);
}