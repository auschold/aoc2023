package d08

import java.io.File

private val nodeRegex = Regex("""^(?<label>[A-Z]{3}) = \((?<left>[A-Z]{3}), (?<right>[A-Z]{3})\)$""")

fun main() {
    val file = File("src/main/resources/d08/input.txt")

    val instructions = file.readLines().first().trim()
    val graph = parseGraph(file.readLines())

    val steps = traverseGraph(graph, instructions)
    println(steps)
}

private fun traverseGraph(graph: Graph, instructions: String): Int {
    var location = "AAA"
    var counter = 0
    var instruction = Instruction(instructions)

    while (location != "ZZZ") {
        val node = graph[location]!!
        location = if (instruction.currentInstruction() == 'L') {
            node.first
        } else {
            node.second
        }
        counter++
        instruction = instruction.nextInstruction()
    }

    return counter
}

private fun parseGraph(lines: List<String>): Graph {
    return lines
        .filter { it.matches(nodeRegex) }
        .associate {
            val match = nodeRegex.matchEntire(it)!!
            match.groups["label"]!!.value to (match.groups["left"]!!.value to match.groups["right"]!!.value)
        }
}

private typealias Graph = Map<String, Pair<String, String>>

private data class Instruction(
    val rawInstructions: String,
    val currentInstructionIndex: Int = 0
) {
    fun currentInstruction() = rawInstructions[currentInstructionIndex]

    fun nextInstruction() =
        if (currentInstructionIndex == rawInstructions.indices.last) {
            Instruction(rawInstructions, 0)
        } else {
            Instruction(rawInstructions, currentInstructionIndex + 1)
        }
}