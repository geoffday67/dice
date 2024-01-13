package uk.co.sullenart.dice.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import timber.log.Timber
import uk.co.sullenart.dice.Config
import java.util.UUID

@SuppressLint("MissingPermission")
class SettingsViewModel(
    val context: Context,
) : ViewModel() {
    var connectionState by mutableStateOf(ConnectionState.DISCONNECTED)

    val options = mutableStateListOf(
        Config.Dice, Config.Timer(10), Config.Coin,
    )
    var currentOption: Config by mutableStateOf(Config.Dice)

    private val adapter = context.getSystemService(BluetoothManager::class.java).adapter
    private val scanResultHandler = ScanResultHandler()
    private var diceGatt: BluetoothGatt? by mutableStateOf(null)
    private var foundServer = false

    private val gattHandler = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    TASK_TYPE_UUID -> {
                        val result = value[0].toInt()
                        currentOption = options.first { it.id == result }
                        Timber.d("Task type characteristic read [$result], initial option [${currentOption.name}]")
                        gatt.readCharacteristic(gatt.getService(SERVICE_UUID).getCharacteristic(TIMER_DURATION_UUID))
                    }

                    TIMER_DURATION_UUID -> {
                        val result = (value[1] * 256) + value[0]
                        Timber.d("Timer duration characteristic read [$result]")
                        options
                            .filterIsInstance<Config.Timer>()
                            .forEach { it.duration = result }
                        connectionState = ConnectionState.CONNECTED
                    }
                }
            } else {
                Timber.w("Error $status during characteristic read")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    TASK_TYPE_UUID -> {
                        Timber.d("Task type characteristic written [${characteristic.uuid}]")
                        (currentOption as? Config.Timer)?.let { option ->
                            val duration = option.duration
                            Timber.d("Saving timer option duration [$duration]")
                            writeCharacteristic(
                                gatt = gatt,
                                characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(TIMER_DURATION_UUID),
                                value = arrayOf((duration % 256).toByte(), (duration / 256).toByte()),
                            )
                        }
                    }

                    TIMER_DURATION_UUID -> {
                        Timber.d("Timer duration characteristic written [${characteristic.uuid}]")
                    }
                }
            } else {
                Timber.w("Error $status during characteristic write")
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Timber.d("GATT connected")
                    diceGatt = gatt
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.d("GATT disconnected")
                    diceGatt = null
                    connectionState = ConnectionState.DISCONNECTED
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Timber.d("${gatt.services.size} services discovered")
            gatt.readCharacteristic(gatt.getService(SERVICE_UUID).getCharacteristic(TASK_TYPE_UUID))
        }
    }

    inner class ScanResultHandler : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!foundServer) {
                Timber.d("Found [${result.device.name}]")
                adapter.bluetoothLeScanner.stopScan(scanResultHandler)
                result.device.connectGatt(context, false, gattHandler)
                foundServer = true
            }
        }
    }

    fun connect() {
        foundServer = false
        connectionState = ConnectionState.CONNECTING

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        adapter.bluetoothLeScanner.startScan(
            listOf(filter),
            ScanSettings.Builder().build(),
            scanResultHandler,
        )
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: Array<Byte>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(characteristic, value.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            characteristic.setValue(value.toByteArray())
            gatt.writeCharacteristic(characteristic)
        }
    }

    fun onSelect(option: Config) {
        Timber.d("Setting current option to [${option.name}]")
        currentOption = option
    }

    fun onDurationChange(duration: Int) {
        Timber.d("Setting timer duration to [$duration]")
        options
            .filterIsInstance<Config.Timer>()
            .forEach { it.duration = duration }
    }

    fun onSave() {
        diceGatt?.let { gatt ->
            Timber.d("Saving current option [${currentOption.name}]")
            writeCharacteristic(
                gatt = gatt,
                characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(TASK_TYPE_UUID),
                value = arrayOf(currentOption.id.toByte() ?: 0),
            )
        }
    }

    companion object {
        private val SERVICE_UUID = UUID.fromString("0220702e-0895-47cc-bb35-d2df06d17041")
        private val TASK_TYPE_UUID = UUID.fromString("bd37f3bf-8f79-42ba-8532-fbd0140c2790")
        private val TIMER_DURATION_UUID = UUID.fromString("4a89e16b-c11a-471f-9b07-cfe736837e47")
    }
}