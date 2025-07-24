package com.mouse.nearclip

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    private var permissionGranted: Boolean = false;
    // 替换成你的设备信息
    private val targetDeviceName = "NearClip" // ← 你的 BLE 外设名称
    private val serviceUUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb") // 电池服务
    private val characteristicUUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb") // 电池电量

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionsIfNeeded()



        findViewById<Button>(R.id.scanButton).setOnClickListener {
            onClickScanButton()
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun onClickScanButton() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Log.d(TAG, "设备: ${result.device?.name} - ${result.device?.address}")

            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "扫描失败: $errorCode")
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothLeScanner.startScan(scanCallback)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter
            permissionGranted = true;
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}