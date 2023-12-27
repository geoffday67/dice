package uk.co.sullenart.dice.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sullenart.dice.Config

@Composable
fun SettingsScreen(
    options: List<Config>,
    currentOptionId: Int?,
    onSelect: (Config) -> Unit,
) {
    Column {
        Divider()
        options.forEach {
            Option(
                option = it,
                isSelected = it.id == (currentOptionId ?: -1),
                onSelect = onSelect,
            )
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
            Text(option.name)
        }
        Divider()
    }
}
