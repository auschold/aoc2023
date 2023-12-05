package d05

import java.io.File

fun main() {
    val file = File("src/main/resources/d05/input.txt")

    val almanac = parseAlmanac(file.readLines())

    val minimumLocation = almanac.seeds
        .minOfOrNull { almanac.lookupLocationForSeed(it) }

    println(minimumLocation)
}

private fun parseAlmanac(lines: List<String>): Almanac {
    val seeds = lines.first { it.startsWith("seeds:") }
        .substringAfter(":")
        .split(" ")
        .filterNot { it.isBlank() }
        .map { it.toLong() }

    val maps = listOf(
        parseMap(lines, "seed-to-soil"),
        parseMap(lines, "soil-to-fertilizer"),
        parseMap(lines, "fertilizer-to-water"),
        parseMap(lines, "water-to-light"),
        parseMap(lines, "light-to-temperature"),
        parseMap(lines, "temperature-to-humidity"),
        parseMap(lines, "humidity-to-location"),
    )

    return Almanac(seeds, maps)
}

private fun parseMap(lines: List<String>, label: String): AlmanacMap {
    val ranges = lines.asSequence()
        .dropWhile { !it.contains(label) }
        .drop(1) // must drop header, too
        .takeWhile { it.isNotBlank() }
        .map { it.split(" ") }
        .map { rawNumbers -> rawNumbers.map { it.toLong() } }
        .map { RangeProjection(LongRange(it[1], it[1] + it[2] - 1), it[0]) }
        .toList()
    return AlmanacMap(ranges)
}

private data class Almanac(
    val seeds: List<Long>,
    val almanacMaps: List<AlmanacMap>
) {
    fun lookupLocationForSeed(seedIndex: Long): Long {
        var result = seedIndex
        almanacMaps.forEach { result = it.lookupProjectedIndex(result) }
        return result
    }
}

private class AlmanacMap(
    val projections: List<RangeProjection>
) {
    fun lookupProjectedIndex(index: Long): Long = projections
        .firstNotNullOfOrNull { it.project(index) }
        ?: index
}

private data class RangeProjection(
    val sourceRange: LongRange,
    val destinationOffset: Long,
) {

    fun project(index: Long): Long? {
        return if (sourceRange.contains(index)) {
            (index - sourceRange.first) + destinationOffset
        } else {
            null
        }
    }
}
