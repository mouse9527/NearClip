package com.mouse.clipboard

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "BLETextApp"

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    // 替换成你的设备信息
    private val targetDeviceName = "BLE_Device" // ← 你的 BLE 外设名称
    private val serviceUUID = UUID.fromString("0000xxxx-0000-1000-8000-00805f9b34fb")
    private val characteristicUUID = UUID.fromString("0000yyyy-0000-1000-8000-00805f9b34fb")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        requestPermissionsIfNeeded()

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            val text = findViewById<EditText>(R.id.inputText).text.toString()
            sendText(text)
        }

        startBleScan()
    }

    private fun requestPermissionsIfNeeded() {
        val requiredPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val notGranted = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 1001)
        }
    }

    private fun startBleScan() {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.name?.contains(targetDeviceName, ignoreCase = true) == true) {
                    Log.d(TAG, "找到目标设备：${device.name}")
                    scanner.stopScan(this)
                    connectToDevice(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "扫描失败: $errorCode")
            }
        }
        scanner.startScan(scanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            return
        } else {
            device.connectGatt(this, false, gattCallback)
        }
        Toast.makeText(this, "正在连接 ${device.name}", Toast.LENGTH_SHORT).show()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "连接成功，开始发现服务")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "连接断开")
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "服务发现完成")
            val service = gatt.getService(serviceUUID)
            val characteristic = service?.getCharacteristic(characteristicUUID)

            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, true)
                Log.d(TAG, "已启用通知")
            } else {
                Log.e(TAG, "未找到特征")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val received = characteristic.value.toString(Charsets.UTF_8)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "收到数据：$received", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendText(text: String) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)

        if (characteristic != null) {
            characteristic.value = text.toByteArray(Charsets.UTF_8)
            val result = gatt.writeCharacteristic(characteristic)
            Log.d(TAG, "发送文本 [$text] 结果：$result")
        } else {
            Log.e(TAG, "未找到可写特征")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}
