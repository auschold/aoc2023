package d08

import java.io.File

private val nodeRegex = Regex("""^(?<label>[0-9A-Z]{3}) = \((?<left>[0-9A-Z]{3}), (?<right>[0-9A-Z]{3})\)$""")

fun main() {
    val file = File("src/main/resources/d08/input.txt")

    val instructions = file.readLines().first().trim()
    val graph = parseGraph(file.readLines())

    val stepsHuman = traverseGraphHuman(graph, instructions)
    println(stepsHuman)

    val stepsGhost = traverseGraphGhost(graph, instructions)
    println(stepsGhost)
}

private fun traverseGraphGhost(graph: Graph, instructions: String): Long =
    graph.keys.filter { it.endsWith("A") }
        .map { traverseGraph(graph, instructions, it) { location -> location.endsWith("Z") } }
        .reduce { a, b -> lcm(a, b) }

private fun traverseGraphHuman(graph: Graph, instructions: String): Long =
    traverseGraph(graph, instructions, "AAA") { it == "ZZZ" }

private fun traverseGraph(graph: Graph, instructions: String, startLocation: String, stopPredicate: (String) -> Boolean): Long {
    var location = startLocation
    var counter = 0L
    var instruction = Instruction(instructions)

    while (!stopPredicate.invoke(location)) {
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

private fun gcd(a: Long, b: Long): Long =
    if (b == 0L) a else gcd(b, a % b)

private fun lcm(a: Long, b: Long) =
    a * (b / gcd(a, b))