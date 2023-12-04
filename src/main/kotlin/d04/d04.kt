package d04

import java.io.File
import kotlin.math.pow

fun main() {
    val file = File("src/main/resources/d04/input.txt")

    val game = Game(file.readLines().map { parseScratchCard(it) })

    val checkSum1 = game.scratchCards.sumOf { it.points() }
    println(checkSum1)

    game.scratchCards.forEachIndexed() { index, card ->
        val matches = card.numberOfMatches()
        val amountOfCard = game.ownedAmountOfCard(index)
        game.addWonCards(index, matches, amountOfCard)
    }

    val checksum2 = game.scratchCards.mapIndexed {
        index, _ -> game.ownedAmountOfCard(index)
    }.sum()
    println(checksum2)
}

private data class Game(
    val scratchCards: List<ScratchCard>
) {
    private val virtualCards: MutableList<Int> by lazy { MutableList(scratchCards.size) { 0 } }

    fun addWonCards(cardIndex: Int, numberOfWonCards: Int, amountOfCardsPerIndex: Int) {
        IntRange(cardIndex + 1, cardIndex + numberOfWonCards).forEach {
            if (it < virtualCards.size) {
                virtualCards[it] = virtualCards[it] + amountOfCardsPerIndex
            }
        }
    }

    fun ownedAmountOfCard(cardIndex: Int) = 1 + virtualCards[cardIndex]
}

private data class ScratchCard (
    val winningNumbers: List<Int>,
    val numbersYouHave: List<Int>,
) {
    fun points(): Int = when (val matches = numberOfMatches()) {
        0 -> 0
        1 -> 1
        else -> 2.0.pow(matches - 1).toInt()
    }

    fun numberOfMatches(): Int = winningNumbers
        .map { numbersYouHave.contains(it) }
        .count { it }
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