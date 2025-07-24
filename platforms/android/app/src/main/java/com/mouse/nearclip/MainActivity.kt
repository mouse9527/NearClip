package com.mouse.nearclip

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val TAG = "NearClip"

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    // 替换成你的设备信息
    private val targetDeviceName = "NearClip" // ← 你的 BLE 外设名称
    private val serviceUUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb") // 电池服务
    private val characteristicUUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb") // 电池电量

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        requestPermissionsIfNeeded()


        findViewById<Button>(R.id.sendButton).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            sendText(findViewById<EditText>(R.id.inputText).text.toString())
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // 所有权限都授予后再开始扫描
            startBleScan()
        } else {
            Toast.makeText(this, "需要权限才能使用蓝牙功能", Toast.LENGTH_LONG).show()
        }
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

        val context = this
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.name?.contains(targetDeviceName, ignoreCase = true) == true) {
                    Log.d(TAG, "找到目标设备：${device.name}")
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // 权限未授予，直接返回或申请权限
                        return
                    }
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
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            return
        } else {
            device.connectGatt(this, false, gattCallback)
        }
        Toast.makeText(this, "正在连接 ${device.name}", Toast.LENGTH_SHORT).show()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "连接成功，开始发现服务")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "连接断开")
                bluetoothGatt = null
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun sendText(text: String) {
        bluetoothGatt?.writeCharacteristic(
            bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)!!,
            text.toByteArray(Charsets.UTF_8),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}