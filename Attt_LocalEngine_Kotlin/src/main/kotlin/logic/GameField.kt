package logic

import elements.Coordinates
import elements.LineDirection
import elements.MAX_GAME_FIELD_SIDE_SIZE
import elements.MIN_GAME_FIELD_SIDE_SIZE
import publicApi.AtttPlayer
import utilities.Log

/**
 * represents the area/space where all players' marks are placed and exist through one active game session
 */
internal class GameField(
    internal var sideLength: Int, // the only required parameter
//    dimensions: Int = MIN_GAME_FIELD_DIMENSIONS // the simplest variant is 2d game
) {
    private val theMap: MutableMap<Coordinates, AtttPlayer> = mutableMapOf() // initially empty to save memory

    init {
        // here we're doing possible corrections that may be needed to keep the game rules reasonable
        if (sideLength > MAX_GAME_FIELD_SIDE_SIZE) sideLength = MAX_GAME_FIELD_SIDE_SIZE
        else if (sideLength < MIN_GAME_FIELD_SIDE_SIZE) sideLength = MIN_GAME_FIELD_SIDE_SIZE
        // let's NOT initialize the initial field for the game to save memory & speed-up new game start
    }

    /**
     * returns beautiful & simple String representation of the current state of game field
     */
    internal fun prepareForPrintingIn2d(): String {
        val sb = StringBuilder(sideLength * (sideLength + 1))
        for (y in 0 until sideLength) {
            sb.append("\n")
            for (x in 0 until sideLength) {
                sb.append(theMap[Coordinates(x, y)]?.getSymbol() ?: SYMBOL_FOR_ABSENT_MARK).append(' ')
            }
        }
        return sb.toString()
    }

    /**
     * allows to see what's inside this game field space for the given coordinates
     */
    internal fun getCurrentMarkAt(x: Int, y: Int): AtttPlayer? = theMap[Coordinates(x, y)]

    private fun containsTheSameMark(what: AtttPlayer?, potentialSpot: Coordinates) = what == theMap[potentialSpot]

    private fun belongToTheSameRealPlayer(givenPlace: Coordinates, potentialSpot: Coordinates): Boolean {
        val newMark = theMap[potentialSpot] // optimization to do finding in map only once
        return newMark != null && newMark != PlayerProvider.None && newMark == theMap[givenPlace]
    }

    /**
     * ensures that the game field has correct size & is clear, so it is safe to use it for a new game
     */
    internal fun isReady(): Boolean =
        sideLength in MIN_GAME_FIELD_SIDE_SIZE..MAX_GAME_FIELD_SIDE_SIZE && theMap.isEmpty()

    internal fun placeNewMark(where: Coordinates, what: AtttPlayer): Boolean =
        if (theMap[where] == null || theMap[where] == PlayerProvider.None) { // PlayerProvider.None - to ensure all cases
            theMap[where] = what
            true // new mark is successfully placed
        } else {
            Log.pl("attempting to set a mark for player $what on the occupied coordinates: $where")
            // later we can also emit a custom exception here - to be caught on the UI side and ask for another point
            false // new mark is not placed because the space has been already occupied
        }

    internal fun detectMaxLineLengthForNewMark(where: Coordinates): Int? =
        detectAllExistingLineDirectionsFromThePlacedMark(where)
            .maxOfOrNull { lineDirection ->
                measureFullLengthForExistingLineFrom(where, lineDirection)
            }

    private fun detectAllExistingLineDirectionsFromThePlacedMark(fromWhere: Coordinates): List<LineDirection> {
        Log.pl("checkPlacedMarkArea: x, y = ${fromWhere.x}, ${fromWhere.y}")
        val checkedMark = getCurrentMarkAt(fromWhere.x, fromWhere.y)
        if (checkedMark == null || checkedMark == PlayerProvider.None) {
            return emptyList() // preventing from doing detection calculations for initially wrong Player
        }
        val allDirections = mutableListOf<LineDirection>()
        LineDirection.entries.filter { it != LineDirection.None }.forEach { lineDirection ->
            val nextCoordinates = fromWhere.getNextInTheDirection(lineDirection)
            if (nextCoordinates.existsWithin(sideLength) && containsTheSameMark(checkedMark, nextCoordinates)) {
                allDirections.add(lineDirection)
                Log.pl("line exists in direction: $lineDirection")
            }
        }
        return allDirections // is empty if no lines ae found in all possible directions
    }

    private fun measureFullLengthForExistingLineFrom(start: Coordinates, lineDirection: LineDirection): Int {
        // here we already have a detected line of 2 minimum dots, now let's measure its full potential length.
        // we also have a proven placed dot of the same player in the detected line direction.
        // so, we only have to inspect next potential dot of the same direction -> let's prepare the coordinates:
        val checkedNearCoordinates = start.getTheNextSpaceFor(lineDirection, sideLength)
        var lineTotalLength = 0
        if (checkedNearCoordinates is Coordinates) {
            lineTotalLength =
                measureLineFrom(checkedNearCoordinates, lineDirection, 2) +
                        measureLineFrom(start, lineDirection.opposite(), 0)
            Log.pl("makeNewMove: lineTotalLength = $lineTotalLength")
        } // else checkedNearCoordinates cannot be Border or anything else apart from Coordinates type
        return lineTotalLength
    }

    internal fun measureLineFrom(givenMark: Coordinates, lineDirection: LineDirection, startingLength: Int): Int {
        Log.pl("measureLineFrom: given startingLength: $startingLength")
        Log.pl("measureLineFrom: given start coordinates: $givenMark")
        // firstly let's measure in the given direction and then in the opposite, also recursively
        val nextMark = givenMark.getTheNextSpaceFor(lineDirection, sideLength)
        Log.pl("measureLineFrom: detected next coordinates: $nextMark")
        return if (nextMark is Coordinates && belongToTheSameRealPlayer(givenMark, nextMark)) {
            measureLineFrom(nextMark, lineDirection, startingLength + 1)
        } else {
            Log.pl("measureLineFrom: ELSE -> exit: $startingLength")
            startingLength
        }
    }
}
