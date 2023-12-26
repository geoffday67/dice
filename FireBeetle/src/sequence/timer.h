#include "sequence.h"

class TimerSequence : public Sequence {
 public:
  TimerSequence(TaskHandle_t);
  void start();

 private:
  static void TaskCode(void*);
  int duration, current;
};