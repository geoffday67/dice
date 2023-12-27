package uk.co.sullenart.dice.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sullenart.dice.Config

@Composable
fun SettingsScreen(
    options: List<Config>,
    onSelect: (Config) -> Unit,
) {
    Column() {
        options.forEach {
            Option(
                option = it,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun Option(
    option: Config,
    onSelect: (Config) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(option) }
            .padding(20.dp),
    ) {
        Text(option.name)
    }
}
