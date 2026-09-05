package com.kshetrajna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kshetrajna.app.ui.foundation.KshetrajnaApp
import com.kshetrajna.app.ui.theme.KshetrajnaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KshetrajnaTheme {
                KshetrajnaApp()
            }
        }
    }
}
