#include <Arduino.h>
#include <NimBLEDevice.h>

#include "nvs.h"
#include "nvs_flash.h"
#include "sequence/coin.h"
#include "sequence/dice.h"
#include "sequence/timer.h"

const int TriggerPin = 22;

const char SERVICE_UUID[] = "0220702e-0895-47cc-bb35-d2df06d17041";
const char TASK_TYPE_UUID[] = "bd37f3bf-8f79-42ba-8532-fbd0140c2790";
const char TIMER_DURATION_UUID[] = "4a89e16b-c11a-471f-9b07-cfe736837e47";

enum TaskType_t {
  Dice,
  Timer,
  Coin,
};

TaskType_t TaskType;
uint16_t TimerDuration;
nvs_handle_t nvsHandle;

class : public NimBLECharacteristicCallbacks {
  virtual void onWrite(NimBLECharacteristic *pcharacteristic) {
    TaskType = (TaskType_t)(pcharacteristic->getValue<uint8_t>());
    nvs_set_u8(nvsHandle, "task_type", (uint8_t)TaskType);
    Serial.printf("Task type now %d\n", (int)TaskType);
  }
} TaskTypeCallbacks;

class : public NimBLECharacteristicCallbacks {
  virtual void onWrite(NimBLECharacteristic *pcharacteristic) {
    TimerDuration = pcharacteristic->getValue<uint16_t>();
    nvs_set_u16(nvsHandle, "timer_duration", TimerDuration);
    Serial.printf("Timer duration now %d\n", TimerDuration);
  }
} TimerDurationCallbacks;

void MainTask(void *pparms) {
  // Watch for trigger, start sequence, wait for sequence to end.
  while (1) {
    if (digitalRead(TriggerPin) == LOW) {
      Sequence *psequence;
      switch (TaskType) {
        case Timer:
          Serial.println("Starting timer");
          psequence = new TimerSequence(xTaskGetCurrentTaskHandle(), TimerDuration);
          break;

        case Dice:
          Serial.println("Starting dice");
          psequence = new DiceSequence(xTaskGetCurrentTaskHandle());
          break;

        case Coin:
          Serial.println("Starting coin");
          psequence = new CoinSequence(xTaskGetCurrentTaskHandle());
          break;
      }
      psequence->start();
      xTaskNotifyWait(0, 0, NULL, portMAX_DELAY);
      Serial.println("Sequence complete");
    }

    taskYIELD();
  }
}

void setup() {
  NimBLEServer *pserver;
  NimBLEService *pservice;
  NimBLECharacteristic *ptask_type, *ptimer_duration;

  Serial.begin(115200);
  Serial.println();
  Serial.println("Starting");

  nvs_flash_init();
  nvs_open("dice", NVS_READWRITE, &nvsHandle);
  Serial.println("NVS initialised");

  if (nvs_get_u8(nvsHandle, "task_type", (uint8_t *)&TaskType) != ESP_OK) {
    TaskType = Dice;
  }
  Serial.printf("Task type %d restored\n", (int)TaskType);

  if (nvs_get_u16(nvsHandle, "timer_duration", &TimerDuration) != ESP_OK) {
    TimerDuration = 10;
  }
  Serial.printf("Timer duration %d restored\n", TimerDuration);

  pinMode(TriggerPin, INPUT_PULLUP);

  NimBLEDevice::init("Dice");
  pserver = NimBLEDevice::createServer();
  pservice = pserver->createService(SERVICE_UUID);

  ptask_type = pservice->createCharacteristic(TASK_TYPE_UUID);
  ptask_type->setValue((int)TaskType);
  ptask_type->setCallbacks(&TaskTypeCallbacks);

  ptimer_duration = pservice->createCharacteristic(TIMER_DURATION_UUID);
  ptask_type->setValue(TimerDuration);
  ptask_type->setCallbacks(&TimerDurationCallbacks);

  pservice->start();

  NimBLEAdvertising *pAdvertising = NimBLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->start();

  xTaskCreatePinnedToCore(MainTask, "MainTask", 8192, NULL, 1, NULL, APP_CPU_NUM);
}

void loop() {
}