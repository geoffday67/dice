package uk.co.sullenart.dice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sullenart.dice.settings.DisconnectedScreen
import uk.co.sullenart.dice.settings.SettingsScreen
import uk.co.sullenart.dice.settings.SettingsViewModel
import uk.co.sullenart.dice.ui.theme.DiceTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewmodel: SettingsViewModel = remember { SettingsViewModel(this) }
            DiceTheme {
                Scaffold(
                    topBar = { TopBar(viewmodel.connected) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .padding(top = 12.dp),
                    ) {
                        if (viewmodel.connected) {
                            SettingsScreen(
                                options = viewmodel.options,
                                onSave = viewmodel::onSave,
                            )
                        } else {
                            DisconnectedScreen(
                                scan = viewmodel::scan,
                                connecting = viewmodel.connecting,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    connected: Boolean,
) {
    TopAppBar(
        title = {
            Column() {
                Text("Dice")
                Text(
                    text = if (connected) "Task type" else "Disconnected",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    )
}
