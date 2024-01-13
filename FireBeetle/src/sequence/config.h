#include "sequence.h"

class ConfigSequence : public Sequence {
 public:
  ConfigSequence(TaskHandle_t);
  void start();

 private:
  static void TaskCode(void*);
};