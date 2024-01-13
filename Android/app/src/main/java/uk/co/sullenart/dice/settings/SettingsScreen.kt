package uk.co.sullenart.dice.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.currentKoinScope
import uk.co.sullenart.dice.Config

@Composable
fun SettingsScreen(
    options: List<Config>,
    currentOption: Config,
    onSelect: (Config) -> Unit,
    onSave: () -> Unit,
    onDurationChange: (Int) -> Unit,
) {
    var saveNeeded by remember { mutableStateOf(false) }

    Column {
        Divider()
        options.forEach {
            Option(
                option = it,
                isSelected = it.id == currentOption.id,
                onSelect = {
                    onSelect(it)
                    saveNeeded = true
                },
            )
            when (it) {
                is Config.Timer -> {
                    TimerExtras(
                        initialValue = it.duration.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { valid ->
                                onDurationChange(valid)
                            }
                            saveNeeded = true
                        }
                    )
                }
                else -> Unit
            }
            Divider()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    onSave()
                    saveNeeded = false
                },
                enabled = saveNeeded,
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun Option(
    option: Config,
    isSelected: Boolean,
    onSelect: (Config) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(option) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelect(option) }
            )
            Text(
                text = option.name,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerExtras(
    initialValue: String,
    onValueChange: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        TextField(
            value = value,
            onValueChange = {
                value = it
                onValueChange(it)
            },
            textStyle = MaterialTheme.typography.titleMedium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            singleLine = true,
        )
    }
}
