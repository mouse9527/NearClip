package com.mouse.nearclip

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private val TAG = "NearClip"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanCallback: ScanCallback? = null
    private val scannedDevices = mutableStateListOf<ScanResult>()

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
                    startBleScan()
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            }) {
                Text("Scan")
            }
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(scannedDevices) { result ->
                    val name = result.device.name ?: "Unknown"
                    Text("$name - ${result.device.address}")
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBleScan() {
        scannedDevices.clear()
        val scanner = bluetoothAdapter.bluetoothLeScanner
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
    }

    override fun onDestroy() {
        scanCallback?.let { bluetoothAdapter.bluetoothLeScanner.stopScan(it) }
        super.onDestroy()
    }
}
