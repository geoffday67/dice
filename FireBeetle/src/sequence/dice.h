#include "sequence.h"

class DiceSequence : public Sequence {
 public:
  DiceSequence(TaskHandle_t task);
  void start();

 private:
  static void TaskCode(void*);
  int target;
  void showNumber(int);
};