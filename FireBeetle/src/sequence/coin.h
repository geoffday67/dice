#include "sequence.h"

class CoinSequence : public Sequence {
 public:
  CoinSequence(TaskHandle_t);
  void start();

 private:
  static void TaskCode(void*);
  bool heads;
};