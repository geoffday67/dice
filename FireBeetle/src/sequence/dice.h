#include "sequence.h"

class DiceSequence : public Sequence {
 public:
  DiceSequence(TaskHandle_t);
  void start();

 private:
  static void TaskCode(void*);
  int maximum, target;
};
