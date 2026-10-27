package com.moa.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moa.finance.ui.navigation.MoaApp
import com.moa.finance.ui.theme.MoaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoaTheme {
                MoaApp()
            }
        }
    }
}
