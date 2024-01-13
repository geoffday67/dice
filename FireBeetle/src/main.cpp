#include <Arduino.h>
#include <NimBLEDevice.h>
#include <SPI.h>

#include "nvs.h"
#include "nvs_flash.h"
#include "output.h"
#include "sequence/coin.h"
#include "sequence/config.h"
#include "sequence/dice.h"
#include "sequence/timer.h"

const EventBits_t ConfigBit = 0x02;
const EventBits_t TriggerBit = 0x01;
const int TriggerPin = 23;

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
TaskHandle_t mainTask;
EventGroupHandle_t diceEventGroup;

void showIdle() {
  Output.setFont(DICE);
  Output.drawCentre('I');
}

class : public NimBLECharacteristicCallbacks {
  virtual void onWrite(NimBLECharacteristic *pcharacteristic) {
    TaskType = (TaskType_t)(pcharacteristic->getValue<uint8_t>());
    nvs_set_u8(nvsHandle, "task_type", (uint8_t)TaskType);

    Serial.printf("Task type now %d\n", (int)TaskType);

    xEventGroupSetBits(diceEventGroup, ConfigBit);
  }
} TaskTypeCallbacks;

class : public NimBLECharacteristicCallbacks {
  virtual void onWrite(NimBLECharacteristic *pcharacteristic) {
    TimerDuration = pcharacteristic->getValue<uint16_t>();
    nvs_set_u16(nvsHandle, "timer_duration", TimerDuration);
    Serial.printf("Timer duration now %d\n", TimerDuration);
  }
} TimerDurationCallbacks;

void IRAM_ATTR TriggerISR() {
  xEventGroupSetBitsFromISR(diceEventGroup, TriggerBit, NULL);
}

void MainTask(void *pparms) {
  bool first, retain;
  Sequence *psequence;
  EventBits_t bits;

  retain = false;
  first = true;

  while (1) {
    xEventGroupClearBits(diceEventGroup, TriggerBit | ConfigBit);
    bits = xEventGroupWaitBits(diceEventGroup, TriggerBit | ConfigBit, pdTRUE, pdFALSE, pdMS_TO_TICKS(500));
    if (bits == 0) {
      // We timed out waiting, update the display if we're not retaining it after the last sequence.
      if (!retain) {
        Output.setFont(DICE);
        if (first) {
          Output.drawCentre('I');
        } else {
          Output.drawCentre('J');
        }
        first = !first;
      }
      continue;
    }

    if ((bits & ConfigBit) != 0) {
      // The config was updated via BLE, start the config confirmation sequence.
      Serial.println("Starting config");
      psequence = new ConfigSequence(xTaskGetCurrentTaskHandle());
    } else {
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
    }

    psequence->start();
    xTaskNotifyWait(0, 0, NULL, portMAX_DELAY);
    retain = psequence->retainDisplay;
    Serial.println("Sequence complete");
  }
}

void setup() {
  NimBLEServer *pserver;
  NimBLEService *pservice;
  NimBLECharacteristic *ptask_type, *ptimer_duration;

  Serial.begin(115200);
  Serial.println();
  Serial.println("Starting");

  diceEventGroup = xEventGroupCreate();

  Output.begin();

  xTaskCreatePinnedToCore(MainTask, "MainTask", 8192, NULL, 1, &mainTask, APP_CPU_NUM);

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

  NimBLEDevice::init("Dice");
  pserver = NimBLEDevice::createServer();
  pservice = pserver->createService(SERVICE_UUID);

  ptask_type = pservice->createCharacteristic(TASK_TYPE_UUID);
  ptask_type->setValue((int)TaskType);
  ptask_type->setCallbacks(&TaskTypeCallbacks);

  ptimer_duration = pservice->createCharacteristic(TIMER_DURATION_UUID);
  ptimer_duration->setValue(TimerDuration);
  ptimer_duration->setCallbacks(&TimerDurationCallbacks);

  pservice->start();

  NimBLEAdvertising *pAdvertising = NimBLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->start();

  pinMode(TriggerPin, INPUT_PULLUP);
  attachInterrupt(TriggerPin, TriggerISR, HIGH);
}

void loop() {
  taskYIELD();
}