package d09

import java.io.File

fun main() {
    val file = File("src/main/resources/d09/input.txt")

    val sequences = file.readLines()
        .map { ValueSequence.of(it) }


    val checksum1 = sequences
        .map { it.extrapolate(false) }
        .sumOf { it.values.last() }
    println(checksum1)

    val checksum2 = sequences
        .map { it.extrapolate(true) }
        .sumOf { it.values.first() }
    println(checksum2)
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

    fun extrapolate(backward: Boolean): ValueSequence {
        if (isAllZeros()) {
            return ValueSequence(values + 0L)
        }

        val extrapolatedDerivation = derivation.extrapolate(backward)

        val extrapolatedValue = if (backward) {
            values.first() - extrapolatedDerivation.values.first()
        } else {
            extrapolatedDerivation.values.last() + values.last()
        }

        return ValueSequence(if (backward) listOf(extrapolatedValue) + values else values + extrapolatedValue)
    }
}