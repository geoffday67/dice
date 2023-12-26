#include <Arduino.h>
#include <NimBLEDevice.h>

#include "nvs_flash.h"
#include "sequence/dice.h"
#include "sequence/timer.h"

const int TriggerPin = 22;

const char SERVICE_UUID[] = "0220702e-0895-47cc-bb35-d2df06d17041";
const char CHARACTERISTIC_UUID[] = "bd37f3bf-8f79-42ba-8532-fbd0140c2790";

enum TaskType_t {
  Dice,
  Timer,
};

TaskType_t TaskType;

class : public NimBLECharacteristicCallbacks {
  virtual void onWrite(NimBLECharacteristic *pCharacteristic) {
    Serial.printf("New value: %s\n", pCharacteristic->getValue().c_str());
  }
} characteristicsCallbacks;

void MainTask(void *pparms) {
  // Watch for trigger, start sequence, wait for sequence to end.
  while (1) {
    if (digitalRead(TriggerPin) == LOW) {
      Sequence *psequence;
      switch (TaskType) {
        case Timer:
          Serial.println("Starting timer");
          psequence = new TimerSequence(xTaskGetCurrentTaskHandle());
          break;

        case Dice:
          Serial.println("Starting dice");
          psequence = new DiceSequence(xTaskGetCurrentTaskHandle());
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
  Serial.begin(115200);
  Serial.println();
  Serial.println("Starting");

  nvs_flash_init();
  Serial.println("NVS initialised");

  pinMode(TriggerPin, INPUT_PULLUP);

  TaskType = Dice;

  NimBLEDevice::init("Dice");
  NimBLEServer *pServer = NimBLEDevice::createServer();
  NimBLEService *pService = pServer->createService(SERVICE_UUID);
  NimBLECharacteristic *pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID);

  pService->start();
  pCharacteristic->setValue("Geoff is great!");
  pCharacteristic->setCallbacks(&characteristicsCallbacks);

  NimBLEAdvertising *pAdvertising = NimBLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->start();

  xTaskCreatePinnedToCore(MainTask, "MainTask", 8192, NULL, 1, NULL, APP_CPU_NUM);
}

void loop() {
}