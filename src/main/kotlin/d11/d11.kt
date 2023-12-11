package d11

import java.io.File

fun main() {
    val file = File("src/main/resources/d11/input.txt")

    val universe = parseUniverse(file.readLines())

    val checksum1 = universe.rawGalaxyCoordinates
        .flatMapIndexed { index: Int, first: Coordinate ->
            universe.rawGalaxyCoordinates.drop(index + 1).map { first to it }
        }.sumOf { universe.getDistanceBetween(it.first, it.second, expansionFactor = 2) }

    println(checksum1)

    val checksum2 = universe.rawGalaxyCoordinates
        .flatMapIndexed { index: Int, first: Coordinate ->
            universe.rawGalaxyCoordinates.drop(index + 1).map { first to it }
        }.sumOf { universe.getDistanceBetween(it.first, it.second, expansionFactor = 1_000_000) }

    println(checksum2)
}

private fun parseUniverse(lines: List<String>): Universe =
    Universe(lines.flatMapIndexed { row: Int, line: String ->
        line.mapIndexedNotNull { column, c ->
            if (c == '#') Coordinate(column, row) else null
        }
    })

private data class Universe(
    val rawGalaxyCoordinates: List<Coordinate>
) {
    fun isRowEmpty(y: Int) = !rawGalaxyCoordinates.any { it.y == y }
    fun isColumnEmpty(x: Int) = !rawGalaxyCoordinates.any { it.x == x }

    fun getDistanceBetween(a: Coordinate, b: Coordinate, expansionFactor: Long): Long {
        val distX = IntRange.from(a.x, b.x).sumOf { if (isColumnEmpty(it)) expansionFactor else 1L } - 1
        val distY = IntRange.from(a.y, b.y).sumOf { if (isRowEmpty(it)) expansionFactor else 1L } - 1

        return distX + distY
    }
}

private data class Coordinate(
    val x: Int,
    val y: Int,
)

fun IntRange.Companion.from(a: Int, b: Int) =
    IntRange(a.coerceAtMost(b), a.coerceAtLeast(b))
