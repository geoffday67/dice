package uk.co.sullenart.dice.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TimerViewModel: ViewModel() {
    var duration by mutableStateOf(5)
}