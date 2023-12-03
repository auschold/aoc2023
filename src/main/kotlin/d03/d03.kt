package d03

import java.io.File

val numberRegex = Regex("""\d+""")

fun main() {
    val file = File("src/main/resources/d03/input.txt")

    val schematic = Schematic(file.readLines())

    val numberLabelAdjacentToSymbol = schematic.numberLabel
        .filter { schematic.isNumberLabelAdjacentToAnySymbol(it) }

    val checkSum = numberLabelAdjacentToSymbol
        .sumOf { it.value }

    println(checkSum)
}

class Schematic(
    private val lines : List<String>
) {
    val numberLabel: List<NumberLabel> by lazy { findNumberLabel() }

    private fun findNumberLabel(): List<NumberLabel> =
        lines.flatMapIndexed { row, line ->
            numberRegex.findAll(line).map {
                NumberLabel(
                    value = it.value.toInt(),
                    row = row,
                    range = it.range
                )
            }
        }

    fun isNumberLabelAdjacentToAnySymbol(numberLabel: NumberLabel): Boolean {
        val widthRange = IntRange(numberLabel.range.first - 1, numberLabel.range.last + 1)
        val heightRange = IntRange(numberLabel.row - 1, numberLabel.row + 1)

        return widthRange.flatMap { column ->
            heightRange.map { row ->
                charAt(row, column)
            }
        }.filterNotNull()
            .any { isSymbol(it) }
    }

    fun charAt(row: Int, column: Int): Char? =
        lines.getOrNull(row)?.getOrNull(column)
}

fun isSymbol(char: Char): Boolean = when {
    char == '.' -> false
    char.isDigit() -> false
    else -> true
}

data class NumberLabel(
    val value: Int,
    val row: Int,
    val range: IntRange,
)
