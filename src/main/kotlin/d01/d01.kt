package d01

import java.io.File

fun main() {
    val file = File("src/main/resources/d01/input.txt")

    val sum = file.readLines()
        .sumOf { extractCoordinate(it) }

    println(sum)
}

private fun extractCoordinate(line: String): Int {
    val chars = line.toCharArray()
        .filter { it.isDigit() }
    return chars.first().digitToInt() * 10 + chars.last().digitToInt()
}
