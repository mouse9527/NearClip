package com.mouse.clipsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sun.jna.Library
import com.sun.jna.Native

interface ClipCoreBindings : Library {
    fun clip_core_ble_pair(): Int
    fun clip_core_ble_send(msg: String): Int
}

class MainActivity : ComponentActivity() {
    private val bindings: ClipCoreBindings by lazy {
        Native.load("clip_core_bindings", ClipCoreBindings::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var status by remember { mutableStateOf("Not paired") }
            var message by remember { mutableStateOf("") }

            MaterialTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = {
                        status = if (bindings.clip_core_ble_pair() == 0) {
                            "Paired"
                        } else {
                            "Pair failed"
                        }
                    }) {
                        Text("Pair")
                    }

                    Text(status, modifier = Modifier.padding(top = 8.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = { Text("Message") }
                    )

                    Button(
                        onClick = {
                            status = if (bindings.clip_core_ble_send(message) == 0) {
                                "Sent"
                            } else {
                                "Send failed"
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
