package uk.co.sullenart.dice

sealed class Config(
    val id: Byte,
    val name: String,
) {
    object Dice: Config(0, "Dice")
    data class Timer(
        val duration: Int,
    ): Config(1, "Timer")
}

data class SubConfig (
    val name: String,
)