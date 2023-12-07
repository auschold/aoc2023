import java.io.File

fun main() {
    val file = File("src/main/resources/d07/input.txt")

    val handsWithBids= file.readLines()
        .map { it.split(" ") }
        .map { HandWithBid(parseHand(it[0]), it[1].toInt()) }

    val checksum1 = handsWithBids.sortedWith(Comparator.comparing(HandWithBid::hand, handComparator))
        .mapIndexed { index, handWithBid ->
            (index + 1) * handWithBid.bid
        }.sum()
    println(checksum1)

    val checksum2 = handsWithBids.sortedWith(Comparator.comparing(HandWithBid::hand, handWithJokerComparator))
        .mapIndexed { index, handWithBid ->
            (index + 1) * handWithBid.bid
        }.sum()
    println(checksum2)
}

private fun parseHand(input: String): Hand = Hand(input.map { Card.fromSymbol(it) })

private data class HandWithBid(
    val hand: Hand,
    val bid: Int
)

private data class Hand(
    val cards: List<Card>
){
    val type: HandType = HandType.entries.first { it.predicate.invoke(cards) }
    val bestHandWithJoker by lazy { findBestHandUsingJoker(this) }

    fun handString() = cards.map { card -> card.symbol }.joinToString("")

    private fun findBestHandUsingJoker(hand: Hand): Hand {
        val inputs = mutableListOf(hand.handString())
        val possibleSubstitutions = mutableSetOf<String>()

        while (inputs.isNotEmpty()) {
            val next = inputs.removeLast()
            if (next.contains("J")) {
                Card.withoutJoker().forEach {
                    inputs.add(next.replaceFirst('J', it.symbol))
                }
            }
            possibleSubstitutions.add(next)
        }

        val result =  possibleSubstitutions.map { parseHand(it) }
            .reduce { a, b ->
                if (handComparator.compare(a, b) > 0) a else b
            }
        return result
    }

    override fun toString(): String {
        return handString() + " [${type.name}]"
    }
}


private val handComparator: Comparator<Hand> = Comparator.comparing(Hand::type).thenComparator { a, b ->
    a.cards.mapIndexed { index, card -> card to b.cards[index] }
        .map { cardComparator.compare(it.first, it.second) }
        .firstOrNull { it != 0 }
        ?: 0
}

private val handWithJokerComparator: Comparator<Hand> = Comparator.comparing<Hand?, HandType?> { it.bestHandWithJoker.type }
    .thenComparator { a, b -> a.cards.mapIndexed { index, card -> card to b.cards[index] }
        .map { cardWithJokerComparator.compare(it.first, it.second) }
        .firstOrNull { it != 0 }
        ?: 0
    }

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

private enum class Card(val symbol: Char, val jokerOrdinal: Int? = null) {
    _2('2'),
    _3('3'),
    _4('4'),
    _5('5'),
    _6('6'),
    _7('7'),
    _8('8'),
    _9('9'),
    T('T'),
    J('J', -1),
    Q('Q'),
    K('K'),
    A('A');

    companion object {
        fun fromSymbol(symbol: Char): Card =
            Card.entries.first { it.symbol == symbol }

        fun withoutJoker(): List<Card> =
            Card.entries.filter { it != J }
    }
}

private val cardComparator : Comparator<Card> = Comparator.comparing(Card::ordinal)

private val cardWithJokerComparator : Comparator<Card> = Comparator.comparing { it.jokerOrdinal ?: it.ordinal }
