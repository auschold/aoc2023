import java.io.File

fun main() {
    val file = File("src/main/resources/d07/input.txt")

    val handsWithBids : List<HandWithBid> = file.readLines()
        .map { it.split(" ") }
        .map { HandWithBid(parseHand(it[0]), it[1].toInt()) }

    val sortedHandsWithBids = handsWithBids.sortedWith(handWithBidComparator)
    val checksum = sortedHandsWithBids.mapIndexed { index, handWithBid ->
        (index + 1) * handWithBid.bid
    }.sum()

    println(checksum)
}

private fun parseHand(input: String): Hand =
    Hand(input.map { Card.fromSymbol(it) })

private data class HandWithBid(
    val hand: Hand,
    val bid: Int
)

private data class Hand(
    val cards: List<Card>
){
    val type: HandType = HandType.entries.first { it.predicate.invoke(cards) }

    override fun toString(): String {
        return cards.map { card -> card.symbol }.joinToString("") + " [${type.name}]"
    }
}

private val handComparator: Comparator<Hand> = Comparator.comparing(Hand::type).thenComparator { a, b ->
    a.cards.mapIndexed { index, card -> card to b.cards[index] }
        .map { it.first compareTo it.second }
        .firstOrNull { it != 0 }
        ?: 0
}

private val handWithBidComparator: Comparator<HandWithBid> = Comparator.comparing(HandWithBid::hand, handComparator)


private enum class HandType(val predicate: (List<Card>) -> Boolean) {
    HIGH_CARD({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 5
    }),
    ONE_PAIR({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 4
    }),
    TWO_PAIR({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 3 && grouped.filter { it.value.size == 2 }.size == 2
    }),
    THREE_OF_A_KIND({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 3 && grouped.filter { it.value.size == 1 }.size == 2
    }),
    FULL_HOUSE({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 2 && grouped.filter { it.value.size == 2 }.any()
    }),
    FOUR_OF_A_KIND({ cards ->
        val grouped = cards.groupBy { it }
        grouped.size == 2 && grouped.filter { it.value.size == 1 }.any()
    }),
    FIVE_OF_A_KIND({ cards ->
        cards.distinct().size == 1
    }),
}

private enum class Card(val symbol: Char): Comparable<Card> {
    _2('2'),
    _3('3'),
    _4('4'),
    _5('5'),
    _6('6'),
    _7('7'),
    _8('8'),
    _9('9'),
    T('T'),
    J('J'),
    Q('Q'),
    K('K'),
    A('A');

    companion object {
        fun fromSymbol(symbol: Char): Card =
            Card.entries.first { it.symbol == symbol }
    }
}