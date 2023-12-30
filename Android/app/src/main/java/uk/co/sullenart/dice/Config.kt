package uk.co.sullenart.dice

sealed class Config(
    val id: Int,
    val name: String,
) {
    object Dice: Config(0, "Dice")
    data class Timer(
        val duration: Int,
    ): Config(1, "Timer")
    object Coin: Config(2, "Coin")
}
