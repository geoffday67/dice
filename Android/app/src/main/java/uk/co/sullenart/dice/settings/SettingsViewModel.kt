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

    // Available options.
    val options = mutableStateListOf(
        Config.Dice, Config.Timer(10), Config.Coin,
    )

    var initialOptionId: Int? = null

    private val adapter = context.getSystemService(BluetoothManager::class.java).adapter
    private val scanResultHandler = ScanResultHandler()
    private var diceGatt: BluetoothGatt? by mutableStateOf(null)

    private var foundServer = false
    private var knownCharacteristics = 0

    private val gattHandler = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    TASK_TYPE_UUID -> {
                        val result = value[0].toInt()
                        Timber.d("Task type characteristic value [$result]")
                        initialOptionId = result
                    }

                    TIMER_DURATION_UUID -> {
                        val result = value[0].toInt()
                        Timber.d("Timer duration characteristic value [$result]")
                        options
                            .filterIsInstance<Config.Timer>()
                            .toMutableList()
                            .replaceAll { it.copy(duration = result) }
                    }
                }

                if (++knownCharacteristics == settingsCharacteristics.size) {
                    connectionState = ConnectionState.CONNECTED
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.d("Characteristic written [${characteristic.uuid}]")
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
            gatt.services
                .flatMap { it.characteristics }
                .filter { it.uuid in setOf(TASK_TYPE_UUID, TIMER_DURATION_UUID) }
                .forEach {
                    Timber.d("Characteristic discovered [${it.uuid}]")
                    gatt.readCharacteristic(it)
                }
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
        knownCharacteristics = 0
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

    fun onSelect(option: Config) {
    }

    fun onSave(option: Config) {
        diceGatt?.let {
            val service = it.getService(SERVICE_UUID)
            val characteristic = service.getCharacteristic(TASK_TYPE_UUID)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.writeCharacteristic(characteristic, ByteArray(1) { option.id.toByte() }, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                characteristic.setValue(ByteArray(1) { option.id.toByte() })
                it.writeCharacteristic(characteristic)
            }
        }
    }

    companion object {
        private val SERVICE_UUID = UUID.fromString("0220702e-0895-47cc-bb35-d2df06d17041")
        private val TASK_TYPE_UUID = UUID.fromString("bd37f3bf-8f79-42ba-8532-fbd0140c2790")
        private val TIMER_DURATION_UUID = UUID.fromString("4a89e16b-c11a-471f-9b07-cfe736837e47")
        private val settingsCharacteristics = setOf(TASK_TYPE_UUID, TIMER_DURATION_UUID)
    }
}