package d05

import java.io.File

fun main() {
    val file = File("src/main/resources/d05/input.txt")

    val almanac = parseAlmanac(file.readLines())

    val minimumLocation1 = almanac.singleSeeds
        .minOfOrNull { almanac.lookupLocationForSeed(it) }
    println(minimumLocation1)

    val minimumLocation2 = generateSequence(0L) { it + 1 }
        .map { it to almanac.lookupInvertSeedForLocation(it) }
        .filter { almanac.hasSeedInRange(it.second) }
        .first()
    println(minimumLocation2.first)
}

private fun parseAlmanac(lines: List<String>): Almanac {
    val singleSeeds = lines.first { it.startsWith("seeds:") }
        .substringAfter(":")
        .split(" ")
        .filterNot { it.isBlank() }
        .map { it.toLong() }

    val seedRanges = singleSeeds
        .chunked(2)
        .map { LongRange(it[0], it[0] + it[1] - 1) }

    val maps = listOf(
        // may be compact these projections to begin with? should decrease runtime a bit?
        parseMap(lines, "seed-to-soil"),
        parseMap(lines, "soil-to-fertilizer"),
        parseMap(lines, "fertilizer-to-water"),
        parseMap(lines, "water-to-light"),
        parseMap(lines, "light-to-temperature"),
        parseMap(lines, "temperature-to-humidity"),
        parseMap(lines, "humidity-to-location"),
    )

    return Almanac(singleSeeds, seedRanges, maps)
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
    val singleSeeds: List<Long>,
    val seedRanges: List<LongRange>,
    val almanacMaps: List<AlmanacMap>
) {

    fun hasSeedInRange(seedIndex: Long): Boolean =
        seedRanges.map { it.contains(seedIndex) }
            .reduce { a, b -> a || b }

    fun lookupLocationForSeed(seedIndex: Long): Long {
        var result = seedIndex
        almanacMaps.forEach { result = it.lookupProjectedIndex(result) }
        return result
    }

    fun lookupInvertSeedForLocation(locationIndex: Long): Long {
        var result : Long = locationIndex
        almanacMaps.reversed()
            .forEach { result = it.lookupInverseProjectedIndex(result) }
        return result
    }
}

private class AlmanacMap(
    val projections: List<RangeProjection>
) {
    fun lookupProjectedIndex(index: Long): Long = projections
        .firstNotNullOfOrNull { it.project(index) }
        ?: index

    fun lookupInverseProjectedIndex(index: Long): Long = projections
        .firstNotNullOfOrNull { it.inverseProjection(index) }
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

    fun inverseProjection(index: Long): Long? {
        val projected = index - destinationOffset + sourceRange.first
        return if (sourceRange.contains(projected)) {
            projected
        } else {
            null
        }
    }
}
