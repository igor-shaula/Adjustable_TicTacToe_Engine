import kotlin.test.*

class GameTests {

    @BeforeTest
    fun switchLoggingOn() {
        Log.switch(true)
    }

    @Test
    fun test3x3FieldWithMultiplePossibleLines() {
        val gameField = AtttField(3)
        val gameRules = AtttRules(3)
        AtttEngine.prepare(gameField, gameRules)

        // .Xx
        // .xo
        // oxo

        AtttEngine.makeMove(Coordinates(1, 1), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(2, 1), AtttPlayer.B)
        AtttEngine.makeMove(Coordinates(2, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(0, 2), AtttPlayer.B)
        AtttEngine.makeMove(Coordinates(1, 2), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(2, 2), AtttPlayer.B)
        AtttEngine.makeMove(Coordinates(1, 0), AtttPlayer.A)

        assertFalse(AtttEngine.isActive(), "Game should have been won")
        // Would be nice to be able to do this:
        // assertEquals(AtttPlayer.A, AtttEngine.getWinner())
    }

    @Test
    fun gameNotStarted_defaultGameCreated_3x3GameFieldExists() {
        prepareClassic3x3GameField()
        assertTrue(AtttEngine.isActive())
    }

    @Test
    fun having2dField_anyLineDirectionChosen_detectingOppositeDirectionIsCorrect() {
        assertEquals(LineDirection.XpY0, LineDirection.XmY0.opposite())
        assertEquals(LineDirection.XmY0, LineDirection.XpY0.opposite())
        assertEquals(LineDirection.X0Yp, LineDirection.X0Ym.opposite())
        assertEquals(LineDirection.X0Ym, LineDirection.X0Yp.opposite())
        assertEquals(LineDirection.XpYp, LineDirection.XmYm.opposite())
        assertEquals(LineDirection.XmYm, LineDirection.XpYp.opposite())
        assertEquals(LineDirection.XpYm, LineDirection.XmYp.opposite())
        assertEquals(LineDirection.XmYp, LineDirection.XpYm.opposite())
        assertEquals(LineDirection.None, LineDirection.None.opposite())
    }

    /**
     * here we check if an adjacent spot exists for every cell in 3x3 game for every possible direction
     */
    @Test
    fun having3x3Field_1MarkSet_adjacentMarkDetectionLogicIsCorrect() {
        checkTheNextSpotDetectionBlock(Coordinates(0, 0))
        checkTheNextSpotDetectionBlock(Coordinates(0, 1))
        checkTheNextSpotDetectionBlock(Coordinates(0, 2))
        checkTheNextSpotDetectionBlock(Coordinates(1, 0))
        checkTheNextSpotDetectionBlock(Coordinates(1, 1))
        checkTheNextSpotDetectionBlock(Coordinates(1, 2))
        checkTheNextSpotDetectionBlock(Coordinates(2, 0))
        checkTheNextSpotDetectionBlock(Coordinates(2, 1))
        checkTheNextSpotDetectionBlock(Coordinates(2, 2))
    }

    @Test
    fun having3x3Field_2AdjacentMarksAreSetByTheSamePlayer_detectedLineLengthIsCorrect() {
        prepareClassic3x3GameField()
        val firstMark = Coordinates(0, 0)
        val secondMark = Coordinates(1, 0)
        AtttEngine.makeMove(firstMark, AtttPlayer.A)
        AtttEngine.makeMove(secondMark, AtttPlayer.A)
        Log.pl("measuring line from $firstMark in the forward direction:")
        val lengthFromFirstToSecond = AtttEngine.gameField.measureLineFrom(firstMark, LineDirection.XpY0, 1)
        Log.pl("measuring line from $firstMark in the opposite direction:")
        val lengthFromSecondToFirst = AtttEngine.gameField.measureLineFrom(secondMark, LineDirection.XmY0, 1)
        assertEquals(2, lengthFromFirstToSecond)
        assertEquals(2, lengthFromSecondToFirst)
    }

    @Test
    fun having3x3Field_2RemoteMarksAreSetByTheSamePlayer_detectedLineLengthIsCorrect() {
        prepareClassic3x3GameField()
        val firstMark = Coordinates(0, 0)
        val secondMark = Coordinates(2, 0)
        AtttEngine.makeMove(firstMark, AtttPlayer.A)
        AtttEngine.makeMove(secondMark, AtttPlayer.A)
        Log.pl("measuring line from $firstMark in the forward direction:")
        val lengthFromFirstToSecond = AtttEngine.gameField.measureLineFrom(firstMark, LineDirection.XpY0, 1)
        Log.pl("measuring line from $firstMark in the opposite direction:")
        val lengthFromSecondToFirst = AtttEngine.gameField.measureLineFrom(secondMark, LineDirection.XmY0, 1)
        assertEquals(1, lengthFromFirstToSecond)
        assertEquals(1, lengthFromSecondToFirst)
        // 1 here is the given length of one dot on the field - if the mark exists - its min line length is 1, not less
    }

    @Test
    fun having3x3Field_2RemoteMarksOfTheSamePlayerAreConnected_detectedLineLengthIsCorrect() {
        prepareClassic3x3GameField()
        val firstMark = Coordinates(0, 0)
        val secondMark = Coordinates(2, 0)
        val connectingMark = Coordinates(1, 0)
        AtttEngine.makeMove(firstMark, AtttPlayer.A)
        AtttEngine.makeMove(secondMark, AtttPlayer.A)
        AtttEngine.makeMove(connectingMark, AtttPlayer.A)
        Log.pl("measuring line from $firstMark in the forward direction:")
        val lengthFromFirstToSecond = AtttEngine.gameField.measureLineFrom(firstMark, LineDirection.XpY0, 1)
        Log.pl("measuring line from $firstMark in the opposite direction:")
        val lengthFromSecondToFirst = AtttEngine.gameField.measureLineFrom(secondMark, LineDirection.XmY0, 1)
        assertEquals(3, lengthFromFirstToSecond)
        assertEquals(3, lengthFromSecondToFirst)
        // 1 here is the given length of one dot on the field - if the mark exists - its min line length is 1, not less
    }

    @Test
    fun having3x3Field_2AdjacentMarksOfTheSamePlayerAreAddedWithOneMoreMark_detectedLineLengthIsCorrect() {
        prepareClassic3x3GameField()
        val firstMark = Coordinates(0, 0)
        val secondMark = Coordinates(1, 0)
        val oneMoreMark = Coordinates(2, 0)
        AtttEngine.makeMove(firstMark, AtttPlayer.A)
        AtttEngine.makeMove(secondMark, AtttPlayer.A)
        AtttEngine.makeMove(oneMoreMark, AtttPlayer.A)
        Log.pl("measuring line from $firstMark in the forward direction:")
        val lengthFromEdgeToEdge = AtttEngine.gameField.measureLineFrom(firstMark, LineDirection.XpY0, 1)
        Log.pl("measuring line from $firstMark in the opposite direction:")
        val lengthFromEdgeToEdgeOpposite = AtttEngine.gameField.measureLineFrom(oneMoreMark, LineDirection.XmY0, 1)
        assertEquals(3, lengthFromEdgeToEdge)
        assertEquals(3, lengthFromEdgeToEdgeOpposite)
        // 1 here is the given length of one dot on the field - if the mark exists - its min line length is 1, not less
    }

    @Test
    fun having3x3Field_2AdjacentMarksAreSetBySequentialPlayers_noLineIsCreatedForAnyPlayer() {
        prepareClassic3x3GameField()
        val firstMark = Coordinates(0, 0)
        val secondMark = Coordinates(1, 0)
        val firstActivePlayer = AtttEngine.activePlayer // should be player A
        AtttEngine.makeMove(firstMark) // after this line active player is replaced with the next -> B
        Log.pl("measuring line from $firstMark for player: $firstActivePlayer in the forward direction:")
        val lengthForPlayerA = AtttEngine.gameField.measureLineFrom(firstMark, LineDirection.XpY0, 1)
        val secondActivePlayer = AtttEngine.activePlayer // should be player B
        AtttEngine.makeMove(secondMark) // after this line active player is replaced with the next -> A
        Log.pl("measuring line from $secondMark for player: $secondActivePlayer in the forward direction:")
        val lengthForPlayerB = AtttEngine.gameField.measureLineFrom(secondMark, LineDirection.XpY0, 1)
        assertEquals(1, lengthForPlayerA)
        assertEquals(1, lengthForPlayerB)
        AtttEngine.makeMove(Coordinates(2, 0))
        AtttEngine.makeMove(Coordinates(1, 1))
        AtttEngine.makeMove(Coordinates(2, 1))
        AtttEngine.makeMove(Coordinates(1, 2))
        Log.pl(AtttEngine.gameField.prepareForPrintingIn2d())
    }

    @Test
    fun havingOneMarkSetForOnePlayerOn3x3Field_TryToSetMarkForAnotherPlayerInTheSamePlace_previousMarkRemainsUnchanged() {
        prepareClassic3x3GameField()
        val someSpot = Coordinates(1, 1)
        AtttEngine.makeMove(someSpot, AtttPlayer.A)
        AtttEngine.makeMove(someSpot, AtttPlayer.B)
        Log.pl("\ngame field with only one player's mark: ${AtttEngine.gameField.prepareForPrintingIn2d()}")
        assertEquals(AtttPlayer.A, AtttEngine.gameField.getCurrentMarkAt(1, 1))
    }

    @Test
    fun having3x3Field_onlyOnePlayerMarksAreSet_victoryConditionIsCorrect() {
        prepareClassic3x3GameField()
        AtttEngine.makeMove(Coordinates(0, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(1, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(2, 0), AtttPlayer.A)
        // gameField & winning message for player A is printed in the console
    }

    @Test
    fun having3x3Field_realSimulation2PlayersMovesMade_victoryConditionIsCorrect() {
        prepareClassic3x3GameField()
        AtttEngine.makeMove(Coordinates(0, 0))
        AtttEngine.makeMove(Coordinates(1, 0))
        AtttEngine.makeMove(Coordinates(2, 0))
        AtttEngine.makeMove(Coordinates(1, 1))
        AtttEngine.makeMove(Coordinates(2, 1))
        AtttEngine.makeMove(Coordinates(1, 2))
        // gameField & winning message for player B is printed in the console
    }

    @Test
    fun having3x3Field_realSimulation2PlayersShortenedMovesMade_victoryConditionIsCorrect() {
        prepareClassic3x3GameField()
        AtttEngine.makeMove(0, 0)
        AtttEngine.makeMove(1, 0)
        AtttEngine.makeMove(2, 0)
        AtttEngine.makeMove(1, 1)
        AtttEngine.makeMove(2, 1)
        AtttEngine.makeMove(1, 2)
        // gameField & winning message for player B is printed in the console
    }

    @Test
    fun test5x5Field() {
        val gameField = AtttField(5)
        val gameRules = AtttRules(5)
        AtttEngine.prepare(gameField, gameRules)
        Log.pl("\ntest3x3Field: gameEngine ready with given field: ${gameField.prepareForPrintingIn2d()}")
        AtttEngine.makeMove(Coordinates(0, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(1, 0), AtttPlayer.A)
//    GameEngine.makeNewMove(Coordinates(2, 0), WhichPlayer.A) // intentionally commented - it will be used a bit later
        AtttEngine.makeMove(Coordinates(3, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(4, 0), AtttPlayer.A)
        AtttEngine.makeMove(Coordinates(2, 0), AtttPlayer.A) // intentionally placed here to connect 2 segments
    }
}
