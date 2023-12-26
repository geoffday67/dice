#include "sequence.h"

void Sequence::complete() {
  xTaskNotify(listeningTask, 0, eNoAction);
}