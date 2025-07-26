package com.mouse.nearclip

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private val TAG = "NearClip"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanCallback: ScanCallback? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val scannedDevices = mutableStateListOf<ScanResult>()
    private val handler = Handler(Looper.getMainLooper())
    private var scanning by mutableStateOf(false)

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        setContent { BleScreen() }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun BleScreen() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val permissionState = rememberMultiplePermissionsState(permissions)

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(onClick = {
                if (permissionState.allPermissionsGranted) {
                    if (scanning) {
                        stopBleScan()
                    } else {
                        startBleScan()
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            }) {
                Text(if (scanning) "Scanning..." else "Scan")
            }
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(scannedDevices, key = { it.device.address }) { result ->
                    val name = result.device.name ?: "Unknown"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { connectToDevice(result.device) }
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                            Text(result.device.address, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBleScan() {
        if (scanning) return
        scannedDevices.clear()
        val scanner = bluetoothAdapter.bluetoothLeScanner
        scanning = true
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val exists = scannedDevices.any { it.device.address == result.device.address }
                if (!exists) scannedDevices.add(result)
            }
            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "扫描失败: $errorCode")
            }
        }
        scanner.startScan(scanCallback)
        handler.postDelayed({ stopBleScan() }, 5000)
    }

    private fun stopBleScan() {
        scanCallback?.let { bluetoothAdapter.bluetoothLeScanner.stopScan(it) }
        scanCallback = null
        scanning = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            return
        }
        bluetoothGatt?.close()
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "GATT connected")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "GATT disconnected")
                bluetoothGatt = null
            }
        }
    }

    override fun onDestroy() {
        stopBleScan()
        bluetoothGatt?.close()
        super.onDestroy()
    }
}
