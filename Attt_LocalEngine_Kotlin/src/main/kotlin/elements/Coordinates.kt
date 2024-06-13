package elements

open class Coordinates(open val x: Int, open val y: Int, open val z: Int = 0) : GameSpace {

    /**
     * detects if given coordinates are correct in the currently active game field
     */
    internal fun existsWithin(sideLength: Int): Boolean =
        x in 0 until sideLength && y in 0 until sideLength && z in 0 until sideLength
}