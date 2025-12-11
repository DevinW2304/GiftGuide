package com.devin.giftguide.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devin.giftguide.data.remote.NetworkModule
import com.devin.giftguide.ui.main.MainScreen
import com.devin.giftguide.ui.main.MainViewModel
import com.devin.giftguide.ui.main.MainViewModelFactory
import com.devin.giftguide.ui.theme.GiftGuideTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = NetworkModule.api  // your existing Retrofit service

        setContent {
            GiftGuideTheme {
                val vm: MainViewModel = viewModel(
                    factory = MainViewModelFactory(api)
                )

                MainScreen(viewModel = vm)
            }
        }
    }
}
