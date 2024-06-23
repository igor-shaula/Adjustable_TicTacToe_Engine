package geometry.concept3D

import gameLogic.GameField
import geometry.abstractions.Coordinates
import geometry.abstractions.OneMoveProcessing
import players.PlayerModel
import utilities.Log

internal class NearestAreaScanWith3D(private val gameField: GameField) : OneMoveProcessing {

    override fun getMaxLengthAchievedForThisMove(where: Coordinates): Int? {
        if (where !is Coordinates3D) return null
        return detectAllExistingLineDirectionsFromThePlacedMark(where)
            .maxOfOrNull { threeAxesDirection ->
                measureFullLengthForExistingLineFrom(where, threeAxesDirection)
            }
    }

    override fun getCoordinatesFor(x: Int, y: Int, z: Int): Coordinates = Coordinates3D(x, y, z)

    private fun detectAllExistingLineDirectionsFromThePlacedMark(fromWhere: Coordinates3D): List<LineDirectionFor3Axes> {
        val checkedMark = gameField.getCurrentMarkAt(fromWhere)
        if (checkedMark == null || checkedMark == PlayerModel.None) {
            return emptyList() // preventing from doing detection calculations for initially wrong Player
        }
        val allDirections = mutableListOf<LineDirectionFor3Axes>()
        LineDirectionFor3Axes.getAllFromLoops().filter { !it.isNone() }.forEach { threeAxisDirection ->
            val nextCoordinates = fromWhere.getNextInTheDirection(
                threeAxisDirection.xAxisLD, threeAxisDirection.yAxisLD, threeAxisDirection.zAxisLD
            )
            if (nextCoordinates.existsWithin(gameField.sideLength) &&
                gameField.containsTheSameMark(checkedMark, nextCoordinates)
            ) {
                allDirections.add(threeAxisDirection)
                Log.pl("line exists in direction: $threeAxisDirection")
            }
        }
        return allDirections // is empty if no lines ae found in all possible directions
    }

    private fun measureFullLengthForExistingLineFrom(
        start: Coordinates3D, lineDirectionFor3Axes: LineDirectionFor3Axes
    ): Int {
        // here we already have a detected line of 2 minimum dots, now let's measure its full potential length.
        // we also have a proven placed dot of the same player in the detected line direction.
        // so, we only have to inspect next potential dot of the same direction -> let's prepare the coordinates:
        val checkedNearCoordinates = start.getTheNextSpaceFor(
            lineDirectionFor3Axes.xAxisLD,
            lineDirectionFor3Axes.yAxisLD,
            lineDirectionFor3Axes.zAxisLD,
            gameField.sideLength
        )
        var lineTotalLength = 0
        if (checkedNearCoordinates is Coordinates3D) {
            lineTotalLength =
                measureLineFrom(checkedNearCoordinates, lineDirectionFor3Axes, 2) +
                        measureLineFrom(start, lineDirectionFor3Axes.opposite(), 0)
            Log.pl("makeNewMove: lineTotalLength = $lineTotalLength")
        } // else checkedNearCoordinates cannot be Border or anything else apart from Coordinates type
        return lineTotalLength
    }

    private fun measureLineFrom(
        givenMark: Coordinates3D, lineDirectionFor3Axes: LineDirectionFor3Axes, startingLength: Int
    ): Int {
        Log.pl("measureLineFrom: given startingLength: $startingLength")
        Log.pl("measureLineFrom: given start coordinates: $givenMark")
        // firstly let's measure in the given direction and then in the opposite, also recursively
        val nextMark = givenMark.getTheNextSpaceFor(
            lineDirectionFor3Axes.xAxisLD,
            lineDirectionFor3Axes.yAxisLD,
            lineDirectionFor3Axes.zAxisLD,
            gameField.sideLength
        )
        Log.pl("measureLineFrom: detected next coordinates: $nextMark")
        return if (nextMark is Coordinates3D && gameField.belongToTheSameRealPlayer(givenMark, nextMark)) {
            measureLineFrom(nextMark, lineDirectionFor3Axes, startingLength + 1)
        } else {
            Log.pl("measureLineFrom: ELSE -> exit: $startingLength")
            startingLength
        }
    }
}