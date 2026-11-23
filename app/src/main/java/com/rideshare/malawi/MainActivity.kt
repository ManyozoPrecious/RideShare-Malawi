package com.rideshare.malawi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rideshare.malawi.ui.theme.RideShareMalawiTheme
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.navigation.RideShareNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppGraph.initialize(applicationContext)
        setContent {
            RideShareMalawiTheme {
                RideShareNavHost()
            }
        }
    }
}
