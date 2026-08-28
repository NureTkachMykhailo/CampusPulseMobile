package com.mtkach.campuspulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mtkach.campuspulse.ui.CampusPulseApp
import com.mtkach.campuspulse.ui.theme.CampusPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CampusPulseApplication
        setContent {
            CampusPulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusPulseApp(repository = app.repository, sessionStore = app.sessionStore)
                }
            }
        }
    }
}
