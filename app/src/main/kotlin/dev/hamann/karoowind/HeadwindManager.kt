package dev.hamann.karoowind

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import timber.log.Timber
import java.util.UUID

/**
 * Manages BLE connection and speed control for the Wahoo Headwind.
 *
 * BLE protocol based on reverse engineering:
 * https://github.com/dkassen/headwind-ble
 *
 * TODO: Verify these UUIDs against actual Headwind hardware.
 */
class HeadwindManager(private val context: Context) {

    companion object {
        // TODO: Confirm actual Headwind BLE service/characteristic UUIDs
        val SERVICE_UUID: UUID = UUID.fromString("a026ee0b-0a7d-4ab3-97fa-f1500f9feb8b")
        val FAN_SPEED_CHAR_UUID: UUID = UUID.fromString("a026e038-0a7d-4ab3-97fa-f1500f9feb8b")
        const val DEVICE_NAME = "HEADWIND"

        // Heart rate zones → fan speed % mapping
        private val HR_SPEED_MAP = listOf(
            0..109 to 0,    // below zone 2: off
            110..129 to 30, // zone 2: low
            130..149 to 60, // zone 3-4: medium
            150..Int.MAX_VALUE to 100, // zone 5: full
        )
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var gatt: BluetoothGatt? = null
    private var fanSpeedCharacteristic: BluetoothGattCharacteristic? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Timber.i("Found Headwind: ${result.device.address}")
            bluetoothAdapter.bluetoothLeScanner.stopScan(this)
            result.device.connectGatt(context, false, gattCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("BLE scan failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    Timber.i("Connected to Headwind, discovering services")
                    gatt.discoverServices()
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    Timber.w("Disconnected from Headwind")
                    fanSpeedCharacteristic = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                fanSpeedCharacteristic = gatt.getService(SERVICE_UUID)
                    ?.getCharacteristic(FAN_SPEED_CHAR_UUID)
                this@HeadwindManager.gatt = gatt
                Timber.i("Headwind ready, characteristic found: ${fanSpeedCharacteristic != null}")
            }
        }
    }

    fun connect() {
        if (!bluetoothAdapter.isEnabled) {
            Timber.w("Bluetooth is not enabled")
            return
        }
        val filter = ScanFilter.Builder()
            .setDeviceName(DEVICE_NAME)
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        bluetoothAdapter.bluetoothLeScanner.startScan(listOf(filter), settings, scanCallback)
        Timber.i("Scanning for Headwind...")
    }

    fun disconnect() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        fanSpeedCharacteristic = null
    }

    fun setSpeedFromHeartRate(bpm: Int) {
        val speed = HR_SPEED_MAP.firstOrNull { bpm in it.first }?.second ?: 0
        setFanSpeed(speed)
    }

    fun setFanSpeed(percent: Int) {
        val characteristic = fanSpeedCharacteristic ?: run {
            Timber.w("Headwind not connected, cannot set speed")
            return
        }
        val speed = percent.coerceIn(0, 100)
        Timber.d("Setting fan speed: $speed%")
        characteristic.value = byteArrayOf(speed.toByte())
        gatt?.writeCharacteristic(characteristic)
    }
}
