package d06

import java.io.File

fun main() {
    val file = File("src/main/resources/d06/input.txt")

    puzzle1(file)
    puzzle2(file)
}

private fun puzzle1(file: File) {
    val historicRaceRecord = parseHistoricRecord1(file.readLines())

    val betterOutcomes = historicRaceRecord
        .map { findBetterRaceOutcome(it) }

    val checksum1 = betterOutcomes
        .map { it.size }
        .reduce { a, b -> a * b }

    println(checksum1)
}

private fun puzzle2(file: File) {
    val historicRaceRecord = parseHistoricRecord2(file.readLines())
    val betterOutcomes = findBetterRaceOutcome(historicRaceRecord)
    val checksum2 = betterOutcomes.size
    println(checksum2)
}

private fun parseHistoricRecord1(lines: List<String>): HistoricRaceRecord {
    val times = lines.first { it.startsWith("Time:") }
        .substringAfter(":")
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.toLong() }

    val distances = lines.first { it.startsWith("Distance:") }
        .substringAfter(":")
        .split(" ")
        .filter { it.isNotBlank() }
        .map { it.toLong() }

    return times.mapIndexed { index, time -> HistoricRound(time, distances[index])}
}

private fun parseHistoricRecord2(lines: List<String>): HistoricRound {
    val duration = lines.first { it.startsWith("Time:") }
        .substringAfter(":")
        .replace(" ", "")
        .toLong()

    val distance = lines.first { it.startsWith("Distance:") }
        .substringAfter(":")
        .replace(" ", "")
        .toLong()

    return HistoricRound(duration, distance)
}

private fun findBetterRaceOutcome(historic: HistoricRound): List<RacePrediction> =
    LongRange(0, historic.duration)
        .map { predict(historic.duration, it) }
        .filter { it.beats(historic) ?: false }

private fun predict(duration: Long, chargeTime: Long): RacePrediction {
    val distance = ((duration - chargeTime) * chargeTime).coerceAtLeast(0)
    return RacePrediction(duration, chargeTime, distance)
}

private typealias HistoricRaceRecord = List<HistoricRound>

private data class HistoricRound(
    val duration: Long,
    val distance: Long,
)

private data class RacePrediction(
    val duration: Long,
    val chargeTime: Long,
    val distance: Long,
) {
    fun beats(historic: HistoricRound): Boolean? {
        if (this.duration != historic.duration) return null
        return this.distance > historic.distance
    }
}
