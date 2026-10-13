package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.security.AppSecurityFortress
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    // Perform active security hardening verification
    AppSecurityFortress.isDeviceTampered(this)
    setContent {
      MyApplicationTheme {
        MainScreen(modifier = Modifier.fillMaxSize())
      }
    }
  }
}

