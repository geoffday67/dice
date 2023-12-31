#include "sequence.h"

class TimerSequence : public Sequence {
 public:
  TimerSequence(TaskHandle_t task, int duration);
  void start();

 private:
  static void TaskCode(void*);
  int duration, current;
};