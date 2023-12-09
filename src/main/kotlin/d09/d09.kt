package d09

import java.io.File

fun main() {
    val file = File("src/main/resources/d09/input.txt")

    val sequences = file.readLines()
        .map { ValueSequence.of(it) }

    val extrapolatedSequences = sequences
        .map { it.extrapolate() }

    val checksum1 = extrapolatedSequences.sumOf { it.values.last() }
    println(checksum1)
}


private data class ValueSequence(
    val values: List<Long>
) {

    val derivation by lazy { derive() }

    companion object {
        fun of(input: String): ValueSequence =
            ValueSequence(input.split(" ")
                .map { it.toLong() }
            )
    }

    private fun derive(): ValueSequence =
        ValueSequence(values.zipWithNext { a, b -> b - a })

    fun isAllZeros() =
        values.filterNot { it == 0L }.isEmpty()

    fun extrapolate(): ValueSequence {
        if (isAllZeros()) {
            return ValueSequence(values.plus(0L))
        }

        val extrapolatedDerivation = derivation.extrapolate()
        val extrapolatedValue = extrapolatedDerivation.values.last() + this.values.last()
        return ValueSequence(values.plus(extrapolatedValue))
    }
}