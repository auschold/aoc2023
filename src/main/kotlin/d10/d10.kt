package d10

import d10.Direction.*
import d10.PipeSymbol.*
import java.io.File

fun main() {
    val file = File("src/main/resources/d10/input.txt")

    val topology = parseTopology(file.readLines())

    val closedPath = findClosedPipePath(topology)

    val checksum1 = (closedPath.size - 1) / 2
    println(checksum1)
}

private fun parseTopology(lines: List<String>): Topology =
    Topology(lines.flatMapIndexed { y, line ->
            line.mapIndexed { x, symbol ->
                Coordinate(x, y) to PipeSymbol.from(symbol)
            }
        }.toMap()
    )

private fun findClosedPipePath(topology: Topology): Path {
    val start = topology.startTile()
    return Direction.entries.firstNotNullOf {
        followPath(
            topology,
            listOf(start, topology.tileAt(start.coordinate.getNeighbour(it))!!)
        )
    }
}

private tailrec fun followPath(topology: Topology, path: List<Tile>): Path? {
    val tail = path.last()
    val previous = path.getOrNull(path.size - 2)
    val nextTile = tail.symbol.connectionDirections
        .map { it to topology.tileAt(tail.coordinate.getNeighbour(it)) }
        .filter { tail.symbol.connectsTo(it.second!!.symbol, it.first) }
        .map { it.second!! }
        .filterNot { it == previous }
        .firstOrNull() ?: return null

    if (nextTile.symbol == S) return path + nextTile
    return followPath(topology, path + nextTile)
}

private data class Topology(
    private val fields: Map<Coordinate, PipeSymbol>
) {

    fun tileAt(coordinate: Coordinate) = fields[coordinate]?.let { Tile(coordinate, it) }

    fun startTile() = fields.filter { it.value == S }.entries.single().let { Tile(it.key, it.value) }
}

private typealias Path = List<Tile>

private fun Path.joinToString() =
    this.joinToString("\n") { "${it.coordinate} : ${it.symbol}"}

private data class Tile(val coordinate: Coordinate, val symbol: PipeSymbol)

private enum class PipeSymbol(val symbol: Char, val connectionDirections: Set<Direction>) {
    VRT('|', setOf(NORTH, SOUTH)),
    HOZ('-', setOf(WEST, EAST)),
    NE('L', setOf(NORTH, EAST)),
    NW('J', setOf(NORTH, WEST)),
    SW('7', setOf(SOUTH, WEST)),
    SE('F', setOf(SOUTH, EAST)),
    GND('.', setOf()),
    S('S', setOf(NORTH, EAST, SOUTH, WEST));

    fun connectsTo(other: PipeSymbol, direction: Direction) =
        this.connectionDirections.contains(direction) && other.connectionDirections.contains(direction.inverse())

    companion object {
        fun from(symbol: Char) =
            entries.first { it.symbol == symbol }
    }
}

private enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    fun inverse(): Direction =
        when(this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            WEST -> EAST
            EAST -> WEST
        }
}

private data class Coordinate(
    val x: Int,
    val y: Int,
) {
    fun getNeighbour(direction: Direction): Coordinate =
        when (direction) {
            NORTH -> Coordinate(x, y - 1)
            SOUTH -> Coordinate(x, y + 1)
            EAST -> Coordinate(x + 1, y)
            WEST -> Coordinate(x - 1, y)
        }
}
