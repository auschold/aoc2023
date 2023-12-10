package d10

import d10.Direction.*
import d10.PipeSymbol.*
import java.io.File

fun main() {
    val file = File("src/main/resources/d10/input.txt")

    val topology = parseTopology(file.readLines())

    val closedPath = findClosedPipePath(topology)

    val checksum1 = closedPath.size/ 2
    println(checksum1)

    val cleanTopology = cleanTopology(topology, closedPath)
    println(cleanTopology.prettyString())

    val checksum2 = countEnclosedTiles(cleanTopology)
    println(checksum2)
}

private fun countEnclosedTiles(topology: Topology) =
    IntRange(0, topology.numberOfRows())
        .map { topology.row(it) }.sumOf { countEnclosedTiles(it) }

private fun countEnclosedTiles(row: List<Tile>): Int {
    var inside = false
    var count = 0
    val consecutivePipes = mutableListOf<Tile>()

    // Walk each row from west to east. By definition, we are outside at the start of each row.
    // Everytime we have to climb over a pipe, we leave or enter the "inside" of the closed pipe path.
    // If we run parallel to a pipe, we do not climb over it. Thus, if a pipe comes from north, is a few fields
    // parallel (west -> east) and then goes again to north, we do not need to climb over it and thus the inside
    // status does not change.

    for (tile in row) {
        val isPipe = tile.symbol.isPipe

        if (isPipe) {
            val towardsEast = tile.symbol.connectionDirections.contains(EAST)
            val towardsNorth = tile.symbol.connectionDirections.contains(NORTH)

            if (towardsEast) {
                consecutivePipes.add(tile)
            } else {
                val wasFirstTowardsNorth = consecutivePipes.firstOrNull()
                    ?.let { it.symbol.connectionDirections.contains(NORTH) }
                    ?: false

                if (wasFirstTowardsNorth != towardsNorth) {
                    inside = !inside
                }
                consecutivePipes.clear()
            }
        } else if (inside) {
            count++
        }

    }

    return count
}

private fun cleanTopology(topology: Topology, path: Path) =
    Topology(topology.tiles()
        .map { tile ->
            if (tile.symbol == S) {
                virtualStart(path)
            } else {
                if (path.containsCoordinate(tile.coordinate)) tile else Tile(tile.coordinate, GND)
            }
        }
        .toList()
    )

private fun virtualStart(path: Path): Tile {
    val firstDirection = path.first().coordinate.getNeighbourDirection(path[1].coordinate)!!
    val secondDirection = path.last().coordinate.getNeighbourDirection(path.first().coordinate)!!

    val virtual = PipeSymbol.entries
        .filter { it.isPipe }
        .filter { S.connectsTo(it, firstDirection.inverse()) }
        .filter { it.connectsTo(S, secondDirection.inverse()) }

    return Tile(path.first().coordinate, virtual.first())
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

    return Direction.entries
        .mapNotNull { nextTileInDirection(topology, start, it) }
        .firstNotNullOf {
            followPath(
                topology,
                listOf(start, it)
            )
        }
}

private tailrec fun followPath(topology: Topology, path: List<Tile>): Path? {
    val tail = path.last()
    val previous = path.getOrNull(path.size - 2)
    val nextTile = tail.symbol.connectionDirections
        .map { nextTileInDirection(topology, tail, it) }
        .filterNot { it == previous }
        .firstOrNull() ?: return null

    if (nextTile.symbol == S) return path
    return followPath(topology, path + nextTile)
}

private fun nextTileInDirection(topology: Topology, tile: Tile, direction: Direction): Tile? =
    topology.tileAt(tile.coordinate.getNeighbour(direction))?.let { next ->
        if (tile.symbol.connectsTo(next.symbol, direction)) next else null
    }

private data class Topology(
    private val fields: Map<Coordinate, PipeSymbol>
) {
    constructor(tiles: List<Tile>): this(tiles.associate { it.coordinate to it.symbol })

    fun tileAt(coordinate: Coordinate) = fields[coordinate]?.let { Tile(coordinate, it) }

    fun numberOfRows() = fields.maxBy { it.key.y }.key.y

    fun row(y: Int) = fields.keys
        .filter { it.y == y }
        .sortedBy { it.x }
        .map { tileAt(it)!! }

    fun tiles() = fields.keys.asSequence()
        .map { tileAt(it)!! }

    fun startTile() = fields.filter { it.value == S }.entries
        .singleOrNull()
        ?.let { Tile(it.key, it.value) }
        ?: tileAt(fields.keys.first())!!

    fun prettyString(): String {
        val sb = StringBuilder()

        IntRange(0, numberOfRows()).map { rowNo ->
            row(rowNo).joinToString("") { it.symbol.prettySymbol.toString() }
        }.forEach { sb.append(it).append("\n") }

        return sb.toString()
    }
}

private typealias Path = List<Tile>

private fun Path.containsCoordinate(coord: Coordinate): Boolean =
    this.any { it.coordinate == coord }

private data class Tile(val coordinate: Coordinate, val symbol: PipeSymbol)

private enum class PipeSymbol(val symbol: Char, val connectionDirections: Set<Direction>, val prettySymbol: Char, val isPipe: Boolean = true) {
    VRT('|', setOf(NORTH, SOUTH), '┃'),
    HOZ('-', setOf(WEST, EAST), '━'),
    NE('L', setOf(NORTH, EAST), '┗'),
    NW('J', setOf(NORTH, WEST), '┛'),
    SW('7', setOf(SOUTH, WEST), '┓'),
    SE('F', setOf(SOUTH, EAST), '┏'),
    GND('.', setOf(), 'x', isPipe = false),
    S('S', setOf(NORTH, EAST, SOUTH, WEST), 'S', isPipe = false);

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

    fun getNeighbourDirection(other: Coordinate): Direction? =
        Direction.entries
                .map { it to this.getNeighbour(it) }
                .singleOrNull { it.second == other }
                ?.first
}
