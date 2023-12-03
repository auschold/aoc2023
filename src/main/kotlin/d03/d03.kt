package d03

import java.io.File

val numberRegex = Regex("""\d+""")

fun main() {
    val file = File("src/main/resources/d03/input.txt")

    val schematic = Schematic(file.readLines())

    val numberLabelAdjacentToSymbol =
        schematic.numberLabel.filter { nl ->
            schematic.symbols.any { s -> nl.isAdjacentTo(s.row, s.column) }
        }

    val checkSum1 = numberLabelAdjacentToSymbol
        .sumOf { it.value }

    println(checkSum1)

    val gearsWithLabels = schematic.symbols
        .filter { isGear(it.value) }
        .map { g ->
            g to schematic.numberLabel.filter { nl -> nl.isAdjacentTo(g.row, g.column) }
        }.filter { it.second.size == 2 }
            .toMap()

    val checkSum2 =
        gearsWithLabels
            .map { it.value[0].value * it.value[1].value }
            .sum()

    println(checkSum2)
}

class Schematic(
    private val lines : List<String>
) {
    val numberLabel: List<NumberLabel> by lazy { findNumberLabel() }
    val symbols: List<Symbol> by lazy { findSymbols() }

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

    private fun findSymbols(): List<Symbol> =
        lines.flatMapIndexed { row, line ->
            line.mapIndexed() { column, char ->
                Symbol(
                    value = char,
                    row = row,
                    column = column
                )
            }
        }.filter { isSymbol(it.value) }
}

fun isSymbol(char: Char): Boolean = when {
    char == '.' -> false
    char.isDigit() -> false
    else -> true
}

fun isGear(char: Char): Boolean = char == '*'

data class Symbol(
    val value: Char,
    val row: Int,
    val column: Int,
)

data class NumberLabel(
    val value: Int,
    val row: Int,
    val range: IntRange,
) {
    fun isAdjacentTo(row: Int, column: Int) =
        IntRange(this.row - 1, this.row + 1).contains(row)
                && IntRange(range.first - 1, range.last + 1).contains(column)
}
