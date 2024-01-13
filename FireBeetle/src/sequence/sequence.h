#pragma once

#include <Arduino.h>

#include "freertos/task.h"

class Sequence {
 public:
  Sequence(TaskHandle_t task) : listeningTask(task) {}
  virtual void start();
  bool retainDisplay;

 private:
  TaskHandle_t listeningTask;

 protected:
  void complete();
};