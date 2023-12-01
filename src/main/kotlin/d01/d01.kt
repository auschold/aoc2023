package d01

import java.io.File
import java.util.stream.IntStream

val digitRegex = Regex("(one|two|three|four|five|six|seven|eight|nine|[0-9])")

fun main() {
    val file = File("src/main/resources/d01/input.txt")

    val sum = file.readLines()
        .sumOf { extractCoordinate(it) }

    println(sum)
}

private fun extractCoordinate(line: String): Int {

    // dttwonezbgmcseven5seven
    //   ^ ^
    // Two and one are mixed here! If this occurs at the end, the last digit is wrong. Thus no regex.findAll

    val digits = IntRange(0, line.length)
        .map { digitRegex.find(line, it) }
        .filterNotNull()
        .map { it.value }
        .map { toNumber(it) }
        .toList()

    return digits.first() * 10 + digits.last()
}

private fun toNumber(s: String): Int =
    when(s) {
        "one" -> 1
        "two" -> 2
        "three" -> 3
        "four" -> 4
        "five" -> 5
        "six" -> 6
        "seven" -> 7
        "eight" -> 8
        "nine" -> 9
        else -> s.toInt()
    }
