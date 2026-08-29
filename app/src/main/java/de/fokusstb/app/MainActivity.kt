package de.fokusstb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import de.fokusstb.app.data.LernplanLoader
import de.fokusstb.app.data.StateStore
import de.fokusstb.app.ui.FokusStBApp
import de.fokusstb.app.ui.theme.FokusStBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val data = LernplanLoader.load(applicationContext)
        val store = StateStore(applicationContext)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return AppViewModel(data, store) as T
            }
        }

        setContent {
            FokusStBTheme {
                val viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                FokusStBApp(viewModel)
            }
        }
    }
}
