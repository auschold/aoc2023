package d12

import java.io.File
import java.util.concurrent.CompletableFuture

fun main() {
    val file = File("src/main/resources/d12/input.txt")

    val inventories = parseInventory(file.readLines())

    val checksum1 = inventories.sumOf { countValidCombinations(it) }
    println(checksum1)

    val checksum2 = inventories
        .map { it.unfold() }
        .map { CompletableFuture.supplyAsync { countValidCombinations(it) } }
        .sumOf { it.join() }
    println(checksum2)
}

private fun countValidCombinations(inventory: Inventory) =
    countValidCombinations(inventory, 0, 0, 0, mutableMapOf())

private fun countValidCombinations(inventory: Inventory, inputOffset: Int, consecutiveBrokenSprings: Int, seqOffset: Int, lookup: MutableMap<LookupKey, Long>): Long {
    val key = LookupKey(inputOffset, consecutiveBrokenSprings, seqOffset)
    if (lookup.containsKey(key)) return lookup[key]!!

    val next = inventory.inputAt(inputOffset)

    if (next == '.') {
        return treatAsGoodSpring(inventory, inputOffset, consecutiveBrokenSprings, seqOffset, lookup)
    } else if (next == '#') {
        return treatAsBrokenSpring(inventory, inputOffset, consecutiveBrokenSprings, seqOffset, lookup)
    } else if (next == '?') {
        val goodPathCount = treatAsGoodSpring(inventory, inputOffset, consecutiveBrokenSprings, seqOffset, lookup)
        val brokenPathCount = treatAsBrokenSpring(inventory, inputOffset, consecutiveBrokenSprings, seqOffset, lookup)
        val result = goodPathCount + brokenPathCount
        lookup.putIfAbsent(key, result)
        return result
    } else {
        // next == null
        if (consecutiveBrokenSprings > 0) return 0 // An unmatched sequence of broken springs -> illegal path
        if (inventory.seqAt(seqOffset) != null) return 0 // Still unmatched sequences -> illegal path
        lookup.putIfAbsent(key, 1)
        return 1
    }
}

private fun treatAsGoodSpring(inventory: Inventory, inputOffset: Int, consecutiveBrokenSprings: Int, seqOffset: Int, lookup: MutableMap<LookupKey, Long>): Long {
    if (consecutiveBrokenSprings > 0) return 0 // Started sequence of broken springs is too short -> illegal path
    return countValidCombinations(inventory, inputOffset + 1, consecutiveBrokenSprings = 0, seqOffset, lookup)
}

private fun treatAsBrokenSpring(inventory: Inventory, inputOffset: Int, consecutiveBrokenSprings: Int, seqOffset: Int, lookup: MutableMap<LookupKey, Long>): Long {
    val requiredSeqLength = inventory.seqAt(seqOffset) ?: return 0

    if (consecutiveBrokenSprings + 1 == requiredSeqLength) {
        // Matched sequence! Next element after current element must be a good spring or EOL!
        val next = inventory.inputAt(inputOffset + 1)
        if (next == null) {
            if (inventory.seqAt(seqOffset + 1) == null) return 1 // Sequence of broken springs at the end of line, no more missing sequences
            return 0 // Sequence of broken springs at the end of line, at least one sequence missing
        }
        return if (next == '.' || next == '?')
            countValidCombinations(inventory, inputOffset + 2, consecutiveBrokenSprings = 0, seqOffset + 1, lookup)
        else 0
    } else
        return countValidCombinations(inventory, inputOffset + 1, consecutiveBrokenSprings + 1, seqOffset, lookup)
}

private fun parseInventory(lines: List<String>): List<Inventory> =
    lines.map { it.split(" ") }
        .map { it[0] to it[1].split(",").map { s -> s.toInt() } }
        .map { Inventory(it.first, it.second) }


private data class Inventory(
    private val input: String,
    private val sequences: List<Int>
) {
    fun inputAt(offset: Int) = input.getOrNull(offset)
    fun seqAt(offset: Int) = sequences.getOrNull(offset)

    fun unfold(): Inventory {
        val unfoldedInput = IntRange(1, 5).joinToString("?") { input }
        val unfoldedSequences = IntRange(1, 5).map { sequences }
            .reduce { a, b -> a + b }

        return Inventory(unfoldedInput, unfoldedSequences)
    }
}

private data class LookupKey(val inputOffset: Int, val consecutiveBrokenSprings: Int, val seqOffset: Int)
