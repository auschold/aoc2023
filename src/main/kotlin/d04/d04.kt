package d04

import java.io.File
import kotlin.math.pow

fun main() {
    val file = File("src/main/resources/d04/input.txt")

    val scratchCards = file.readLines()
        .map { parseScratchCard(it) }

    val checkSum = scratchCards.sumOf { it.points() }
    println(checkSum)
}

private data class ScratchCard (
    val winningNumbers: List<Int>,
    val numbersYouHave: List<Int>,
) {
    fun points(): Int {
        val numberOfMatches = winningNumbers.map { numbersYouHave.contains(it) }
            .count { it }

        return when (numberOfMatches) {
            0 -> 0
            1 -> 1
            else -> 2.0.pow(numberOfMatches - 1).toInt()
        }

    }
}

private fun parseScratchCard(line: String): ScratchCard {
    val winningNumbers = line.substringAfter(":").substringBefore("|")
        .split(" ")
        .filter { it.isNotEmpty() }
        .map { it.toInt() }

    val numbersYouHave = line.substringAfter("|")
        .split(" ")
        .filter { it.isNotEmpty() }
        .map { it.toInt() }

    return ScratchCard(winningNumbers, numbersYouHave)
}