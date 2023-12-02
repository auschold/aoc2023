package d02

import java.io.File

val referenceCubeSet = mutableMapOf(
    "red" to 12,
    "green" to 13,
    "blue" to 14,
)

fun main() {
    val file = File("src/main/resources/d02/input.txt")

    val games = file.readLines()
        .mapIndexed { index, line-> Game(index + 1, parseSubeSet(line)) }

    val possibleGames = games
        .filter { isGamePossible(it, referenceCubeSet) }

    val checksum = possibleGames.sumOf { it.id }

    println("Valid games checksum: $checksum")

    val minimumCubeSets = games.map { minimumCubeSet(it) }
    val checksumMinimumCubeSets = minimumCubeSets.sumOf { it.power() }

    println("Minimum cubeset checksum: $checksumMinimumCubeSets")
}

private fun parseSubeSet(line: String): List<CubeSet> =
    line.substringAfter(":")
        .split(";")
        .map { s -> s.split(",").map { it.trim() } }
        .map { toCubeSet(it) }

private fun toCubeSet(input: List<String>): CubeSet =
    input.associate { s ->
        s.split(" ").let { it[1] to it[0].toInt() }
    }.toMutableMap()

private fun minimumCubeSet(game: Game): CubeSet {
    val result: CubeSet = mutableMapOf()

    game.cubeSets
        .flatMap { it.entries }
        .forEach { result.compute(it.key) { _, value ->
            (value ?: 0).coerceAtLeast(it.value)
        } }

    return result
}

private fun isGamePossible(game: Game, reference: CubeSet): Boolean =
    game.cubeSets.filterNot { isCubeSetPossible(it, reference) }
        .isEmpty()

private fun isCubeSetPossible(actual: CubeSet, reference: CubeSet): Boolean =
    actual.map { it.value <= reference[it.key]!! }
        .reduce { a, b ->  a && b}

private data class Game(
    val id: Int,
    val cubeSets: List<CubeSet>
)

typealias CubeSet = MutableMap<String, Int>

fun CubeSet.power() = this.entries
    .map { it.value }
    .reduce { a, b -> a.coerceAtLeast(1) * b.coerceAtLeast(1) }
