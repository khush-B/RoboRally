package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the GameController covering Assignments 6a, 6c, 6d, and 6e.
 * Aims for 100% code coverage of GameController (excluding assert false
 * and dead code), and 100% coverage of doAction() in ConveyorBelt and
 * Checkpoint.
 */
class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null, "Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    // ---------------------------------------------------------------
    //  Assignment 6a: click-to-move
    // ---------------------------------------------------------------

    /** Tests that click-to-move places the player and advances the turn. */
    @Test
    void testV1() {
        Board board = gameController.board;
        Player player1 = board.getCurrentPlayer();
        Player player2 = board.getPlayer(1);
        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

        Assertions.assertEquals(player1, board.getSpace(0, 4).getPlayer(),
                "Player " + player1.getName() + " should be on Space (0,4)!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(),
                "Space (0,0) should be empty!");
        Assertions.assertEquals(player2, board.getCurrentPlayer(),
                "Current player should be " + player2.getName() + "!");
    }

    /** Click on a non-adjacent occupied space should do nothing. */
    @Test
    void clickToMoveNonAdjacentOccupied() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        // (1,1) is diagonal from (0,0), so non-adjacent and occupied by Player 1
        gameController.moveCurrentPlayerToSpace(board.getSpace(1, 1));

        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(),
                "Current player should still be at (0,0)!");
    }

    /** Click-to-move to an adjacent empty space should work. */
    @Test
    void clickToMoveAdjacentEmpty() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        // Player 0 at (0,0), click EAST to (1,0) which is empty
        gameController.moveCurrentPlayerToSpace(board.getSpace(1, 0));

        Assertions.assertEquals(current, board.getSpace(1, 0).getPlayer(),
                "Player should have moved to (1,0)!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(),
                "Space (0,0) should be empty!");
    }

    /** Click-to-move increments the move counter. */
    @Test
    void clickToMoveIncrementsCounter() {
        Board board = gameController.board;
        int before = board.getCounter();
        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

        Assertions.assertEquals(before + 1, board.getCounter(),
                "Counter should be incremented by 1 after move!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6c: command execution
    // ---------------------------------------------------------------

    /** Player 0 faces SOUTH, moveForward should move from (0,0) to (0,1). */
    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(0, 1).getPlayer(),
                "Player " + current.getName() + " should be on Space (0,1)!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(),
                "Player 0 should still be heading SOUTH!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(),
                "Space (0,0) should be empty!");
    }

    /** fastForward should move the player two spaces in their heading direction. */
    @Test
    void fastForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        gameController.fastForward(current);

        Assertions.assertEquals(current, board.getSpace(0, 2).getPlayer(),
                "Player should be on Space (0,2) after fast forward!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(),
                "Original space should be empty!");
    }

    /** turnRight should change heading from SOUTH to WEST. */
    @Test
    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Assertions.assertEquals(Heading.SOUTH, current.getHeading());

        gameController.turnRight(current);

        Assertions.assertEquals(Heading.WEST, current.getHeading(),
                "Player should face WEST after turning right from SOUTH!");
        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(),
                "Player should not have moved!");
    }

    /** turnLeft should change heading from SOUTH to EAST. */
    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Assertions.assertEquals(Heading.SOUTH, current.getHeading());

        gameController.turnLeft(current);

        Assertions.assertEquals(Heading.EAST, current.getHeading(),
                "Player should face EAST after turning left from SOUTH!");
        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(),
                "Player should not have moved!");
    }

    /** moveBackward should move the player one space opposite to heading without changing heading. */
    @Test
    void moveBackward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(3, 3));
        current.setHeading(Heading.SOUTH);

        gameController.moveBackward(current);

        Assertions.assertEquals(current, board.getSpace(3, 2).getPlayer(),
                "Player should be on (3,2) after moving backward!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(),
                "Heading should remain SOUTH after backward!");
    }

    /** uTurn should rotate the player 180 degrees without moving. */
    @Test
    void uTurn() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.SOUTH);

        gameController.uTurn(current);

        Assertions.assertEquals(Heading.NORTH, current.getHeading(),
                "Player should face NORTH after U-turn from SOUTH!");
        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(),
                "Player should not have moved!");
    }

    // ---------------------------------------------------------------
    //  Wall blocking tests
    // ---------------------------------------------------------------

    /** A wall on the current space should block movement in that direction. */
    @Test
    void moveForwardBlockedByWallOnCurrentSpace() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(2, 2));
        current.setHeading(Heading.SOUTH);

        board.getSpace(2, 2).getWalls().add(Heading.SOUTH);

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(2, 2).getPlayer(),
                "Player should NOT have moved — blocked by wall!");
    }

    /** A wall on the target space (opposite side) should block movement. */
    @Test
    void moveForwardBlockedByWallOnTargetSpace() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(2, 2));
        current.setHeading(Heading.SOUTH);

        board.getSpace(2, 3).getWalls().add(Heading.NORTH);

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(2, 2).getPlayer(),
                "Player should NOT have moved — blocked by wall on target space!");
    }

    /** Wall should also block backward movement. */
    @Test
    void moveBackwardBlockedByWall() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(3, 3));
        current.setHeading(Heading.SOUTH);

        board.getSpace(3, 3).getWalls().add(Heading.NORTH);

        gameController.moveBackward(current);

        Assertions.assertEquals(current, board.getSpace(3, 3).getPlayer(),
                "Player should NOT have moved backward — blocked by wall!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6d: robot pushing
    // ---------------------------------------------------------------

    /** Moving into a space occupied by another robot should push it. */
    @Test
    void pushSingleRobot() {
        Board board = gameController.board;
        Player pusher = board.getPlayer(0);
        Player pushed = board.getPlayer(1);

        pusher.setSpace(board.getSpace(3, 3));
        pusher.setHeading(Heading.SOUTH);
        pushed.setSpace(board.getSpace(3, 4));

        gameController.moveForward(pusher);

        Assertions.assertEquals(pusher, board.getSpace(3, 4).getPlayer(),
                "Pusher should be on (3,4)!");
        Assertions.assertEquals(pushed, board.getSpace(3, 5).getPlayer(),
                "Pushed robot should be on (3,5)!");
    }

    /** Pushing a chain of two robots. */
    @Test
    void pushChainOfRobots() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        Player p1 = board.getPlayer(1);
        Player p2 = board.getPlayer(2);

        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p1.setSpace(board.getSpace(3, 4));
        p2.setSpace(board.getSpace(3, 5));

        gameController.moveForward(p0);

        Assertions.assertEquals(p0, board.getSpace(3, 4).getPlayer(),
                "P0 should be at (3,4)!");
        Assertions.assertEquals(p1, board.getSpace(3, 5).getPlayer(),
                "P1 should be pushed to (3,5)!");
        Assertions.assertEquals(p2, board.getSpace(3, 6).getPlayer(),
                "P2 should be pushed to (3,6)!");
    }

    /** Push should fail if blocked by a wall — nobody moves. */
    @Test
    void pushBlockedByWall() {
        Board board = gameController.board;
        Player pusher = board.getPlayer(0);
        Player pushed = board.getPlayer(1);

        pusher.setSpace(board.getSpace(3, 3));
        pusher.setHeading(Heading.SOUTH);
        pushed.setSpace(board.getSpace(3, 4));
        board.getSpace(3, 4).getWalls().add(Heading.SOUTH);

        gameController.moveForward(pusher);

        Assertions.assertEquals(pusher, board.getSpace(3, 3).getPlayer(),
                "Pusher should NOT have moved — push blocked by wall!");
        Assertions.assertEquals(pushed, board.getSpace(3, 4).getPlayer(),
                "Pushed robot should NOT have moved!");
    }

    /** Push during backward movement. */
    @Test
    void pushDuringBackward() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        Player p1 = board.getPlayer(1);

        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH); // backward = NORTH
        p1.setSpace(board.getSpace(3, 2));

        gameController.moveBackward(p0);

        Assertions.assertEquals(p0, board.getSpace(3, 2).getPlayer(),
                "P0 should be at (3,2)!");
        Assertions.assertEquals(p1, board.getSpace(3, 1).getPlayer(),
                "P1 should be pushed to (3,1)!");
    }

    /** Push during backward blocked by wall — nobody moves. */
    @Test
    void pushDuringBackwardBlockedByWall() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        Player p1 = board.getPlayer(1);

        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p1.setSpace(board.getSpace(3, 2));
        board.getSpace(3, 2).getWalls().add(Heading.NORTH);

        gameController.moveBackward(p0);

        Assertions.assertEquals(p0, board.getSpace(3, 3).getPlayer(),
                "P0 should NOT have moved!");
        Assertions.assertEquals(p1, board.getSpace(3, 2).getPlayer(),
                "P1 should NOT have moved!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6d: field actions - ConveyorBelt
    // ---------------------------------------------------------------

    /** ConveyorBelt should move a player one space in the belt's direction. */
    @Test
    void conveyorBeltMovesPlayer() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));

        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(4, 4).getActions().add(belt);

        boolean result = belt.doAction(gameController, board.getSpace(4, 4));

        Assertions.assertTrue(result, "doAction should return true when player is moved!");
        Assertions.assertEquals(player, board.getSpace(5, 4).getPlayer(),
                "Player should be moved EAST to (5,4) by conveyor belt!");
    }

    /** ConveyorBelt should NOT move player if target space is blocked by a wall. */
    @Test
    void conveyorBeltBlockedByWall() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));

        board.getSpace(4, 4).getWalls().add(Heading.EAST);

        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(4, 4).getActions().add(belt);

        boolean result = belt.doAction(gameController, board.getSpace(4, 4));

        Assertions.assertFalse(result, "doAction should return false when blocked by wall!");
        Assertions.assertEquals(player, board.getSpace(4, 4).getPlayer(),
                "Player should NOT move — belt blocked by wall!");
    }

    /** ConveyorBelt on an empty space should return false. */
    @Test
    void conveyorBeltNoPlayer() {
        Board board = gameController.board;

        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(6, 6).getActions().add(belt);

        boolean result = belt.doAction(gameController, board.getSpace(6, 6));

        Assertions.assertFalse(result, "doAction should return false when no player on space!");
    }

    /** ConveyorBelt should NOT move player if target space is occupied. */
    @Test
    void conveyorBeltTargetOccupied() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        Player p1 = board.getPlayer(1);
        p0.setSpace(board.getSpace(4, 4));
        p1.setSpace(board.getSpace(5, 4));

        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(4, 4).getActions().add(belt);

        boolean result = belt.doAction(gameController, board.getSpace(4, 4));

        Assertions.assertFalse(result, "doAction should return false when target is occupied!");
        Assertions.assertEquals(p0, board.getSpace(4, 4).getPlayer(),
                "Player 0 should stay at (4,4)!");
        Assertions.assertEquals(p1, board.getSpace(5, 4).getPlayer(),
                "Player 1 should stay at (5,4)!");
    }

    /** ConveyorBelt getHeading should return the set heading. */
    @Test
    void conveyorBeltGetHeading() {
        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.NORTH);
        Assertions.assertEquals(Heading.NORTH, belt.getHeading());
    }

    // ---------------------------------------------------------------
    //  Assignment 6d: field actions - Checkpoint
    // ---------------------------------------------------------------

    /** Checkpoint should be collected only in order. */
    @Test
    void checkpointCollectedInOrder() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));
        Assertions.assertEquals(0, player.getCheckpoints());

        Checkpoint cp1 = new Checkpoint();
        cp1.setNumber(1);
        board.getSpace(4, 4).getActions().add(cp1);

        boolean result = cp1.doAction(gameController, board.getSpace(4, 4));
        Assertions.assertTrue(result, "doAction should return true when checkpoint collected!");
        Assertions.assertEquals(1, player.getCheckpoints(),
                "Player should have collected checkpoint 1!");
    }

    /** Player should NOT collect checkpoint 2 before checkpoint 1. */
    @Test
    void checkpointSkippedIfOutOfOrder() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));
        Assertions.assertEquals(0, player.getCheckpoints());

        Checkpoint cp2 = new Checkpoint();
        cp2.setNumber(2);
        board.getSpace(4, 4).getActions().add(cp2);

        boolean result = cp2.doAction(gameController, board.getSpace(4, 4));
        Assertions.assertFalse(result, "doAction should return false when checkpoint out of order!");
        Assertions.assertEquals(0, player.getCheckpoints(),
                "Player should NOT collect checkpoint 2 before 1!");
    }

    /** Checkpoints should be collectable sequentially. */
    @Test
    void checkpointSequentialCollection() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);

        Checkpoint cp1 = new Checkpoint();
        cp1.setNumber(1);
        board.getSpace(2, 2).getActions().add(cp1);

        Checkpoint cp2 = new Checkpoint();
        cp2.setNumber(2);
        board.getSpace(3, 3).getActions().add(cp2);

        player.setSpace(board.getSpace(2, 2));
        cp1.doAction(gameController, board.getSpace(2, 2));
        Assertions.assertEquals(1, player.getCheckpoints());

        player.setSpace(board.getSpace(3, 3));
        cp2.doAction(gameController, board.getSpace(3, 3));
        Assertions.assertEquals(2, player.getCheckpoints());
    }

    /** Checkpoint on an empty space should return false. */
    @Test
    void checkpointNoPlayer() {
        Board board = gameController.board;

        Checkpoint cp = new Checkpoint();
        cp.setNumber(1);
        board.getSpace(7, 7).getActions().add(cp);

        boolean result = cp.doAction(gameController, board.getSpace(7, 7));
        Assertions.assertFalse(result, "doAction should return false when no player on space!");
    }

    /** Checkpoint getNumber and isLastCheckpoint. */
    @Test
    void checkpointGettersSetters() {
        Checkpoint cp = new Checkpoint();
        cp.setNumber(3);
        cp.setLastCheckpoint(true);
        Assertions.assertEquals(3, cp.getNumber());
        Assertions.assertTrue(cp.isLastCheckpoint());
    }

    // ---------------------------------------------------------------
    //  Click-to-move: wall and push behavior in GUI
    // ---------------------------------------------------------------

    /** Clicking an adjacent space blocked by a wall should not move the player. */
    @Test
    void clickToMoveBlockedByWall() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(4, 4));

        board.getSpace(4, 4).getWalls().add(Heading.SOUTH);

        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 5));

        Assertions.assertEquals(current, board.getSpace(4, 4).getPlayer(),
                "Player should NOT have moved — adjacent click blocked by wall!");
    }

    /** Clicking an adjacent occupied space should push that robot. */
    @Test
    void clickToMovePushesAdjacentRobot() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Player other = board.getPlayer(1);

        current.setSpace(board.getSpace(4, 4));
        other.setSpace(board.getSpace(4, 5));

        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 5));

        Assertions.assertEquals(current, board.getSpace(4, 5).getPlayer(),
                "Current player should be on (4,5) after pushing!");
        Assertions.assertEquals(other, board.getSpace(4, 6).getPlayer(),
                "Other player should be pushed to (4,6)!");
    }

    /** Clicking an adjacent occupied space should NOT push if wall blocks the push. */
    @Test
    void clickToMovePushBlockedByWall() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        Player other = board.getPlayer(1);

        current.setSpace(board.getSpace(4, 4));
        other.setSpace(board.getSpace(4, 5));
        board.getSpace(4, 5).getWalls().add(Heading.SOUTH);

        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 5));

        Assertions.assertEquals(current, board.getSpace(4, 4).getPlayer(),
                "Current player should NOT have moved — push blocked by wall!");
        Assertions.assertEquals(other, board.getSpace(4, 5).getPlayer(),
                "Other player should NOT have moved!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6e: win condition
    // ---------------------------------------------------------------

    /** Collecting the last checkpoint should set the winner and FINISHED phase. */
    @Test
    void lastCheckpointTriggersWin() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setCheckpoints(2);

        Checkpoint lastCp = new Checkpoint();
        lastCp.setNumber(3);
        lastCp.setLastCheckpoint(true);
        board.getSpace(5, 5).getActions().add(lastCp);

        player.setSpace(board.getSpace(5, 5));
        lastCp.doAction(gameController, board.getSpace(5, 5));

        Assertions.assertEquals(3, player.getCheckpoints());
        Assertions.assertEquals(Phase.FINISHED, board.getPhase(),
                "Phase should be FINISHED after last checkpoint!");
        Assertions.assertEquals(player, board.getWinner(),
                "Winner should be the player who collected the last checkpoint!");
    }

    /** Non-last checkpoint should NOT trigger win. */
    @Test
    void nonLastCheckpointDoesNotTriggerWin() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);

        Checkpoint cp1 = new Checkpoint();
        cp1.setNumber(1);
        cp1.setLastCheckpoint(false);
        board.getSpace(5, 5).getActions().add(cp1);

        player.setSpace(board.getSpace(5, 5));
        cp1.doAction(gameController, board.getSpace(5, 5));

        Assertions.assertEquals(1, player.getCheckpoints());
        Assertions.assertNotEquals(Phase.FINISHED, board.getPhase(),
                "Phase should NOT be FINISHED after non-last checkpoint!");
        Assertions.assertNull(board.getWinner(),
                "Winner should be null after non-last checkpoint!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6e: interactive command card
    // ---------------------------------------------------------------

    /** LEFT_OR_RIGHT command should be interactive. */
    @Test
    void leftOrRightIsInteractive() {
        Assertions.assertTrue(Command.LEFT_OR_RIGHT.isInteractive(),
                "LEFT_OR_RIGHT should be an interactive command!");
        Assertions.assertEquals(2, Command.LEFT_OR_RIGHT.getOptions().size(),
                "LEFT_OR_RIGHT should have 2 options!");
    }

    /** executeCommandOption with LEFT should turn the player left. */
    @Test
    void executeCommandOptionLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.SOUTH);

        current.getProgramField(0).setCard(new CommandCard(Command.LEFT_OR_RIGHT));
        gameController.finishProgrammingPhase();
        // Execute one step — should detect interactive card and switch to PLAYER_INTERACTION
        gameController.executeStep();

        Assertions.assertEquals(Phase.PLAYER_INTERACTION, board.getPhase(),
                "Phase should be PLAYER_INTERACTION for interactive card!");

        // Player chooses LEFT
        gameController.executeCommandOption(Command.LEFT);

        Assertions.assertEquals(Heading.EAST, current.getHeading(),
                "Player should face EAST after choosing LEFT from SOUTH!");
    }

    /** executeCommandOption with RIGHT should turn the player right. */
    @Test
    void executeCommandOptionRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setHeading(Heading.SOUTH);

        current.getProgramField(0).setCard(new CommandCard(Command.LEFT_OR_RIGHT));
        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(Phase.PLAYER_INTERACTION, board.getPhase());

        gameController.executeCommandOption(Command.RIGHT);

        Assertions.assertEquals(Heading.WEST, current.getHeading(),
                "Player should face WEST after choosing RIGHT from SOUTH!");
    }

    // ---------------------------------------------------------------
    //  Programming phase tests (for coverage)
    // ---------------------------------------------------------------

    /** startProgrammingPhase should set phase, deal cards, clear programs. */
    @Test
    void startProgrammingPhase() {
        Board board = gameController.board;
        gameController.startProgrammingPhase();

        Assertions.assertEquals(Phase.PROGRAMMING, board.getPhase());
        Assertions.assertEquals(board.getPlayer(0), board.getCurrentPlayer());
        Assertions.assertEquals(0, board.getStep());

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                Assertions.assertNull(p.getProgramField(j).getCard(),
                        "Program field should be null after startProgrammingPhase!");
                Assertions.assertTrue(p.getProgramField(j).isVisible(),
                        "Program field should be visible!");
            }
            for (int j = 0; j < Player.NO_CARDS; j++) {
                Assertions.assertNotNull(p.getCardField(j).getCard(),
                        "Card field should have a card dealt!");
                Assertions.assertTrue(p.getCardField(j).isVisible(),
                        "Card field should be visible!");
            }
        }
    }

    /** finishProgrammingPhase should hide programs, then show register 0, set ACTIVATION. */
    @Test
    void finishProgrammingPhase() {
        Board board = gameController.board;
        gameController.startProgrammingPhase();
        gameController.finishProgrammingPhase();

        Assertions.assertEquals(Phase.ACTIVATION, board.getPhase());
        Assertions.assertEquals(board.getPlayer(0), board.getCurrentPlayer());
        Assertions.assertEquals(0, board.getStep());

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Assertions.assertTrue(board.getPlayer(i).getProgramField(0).isVisible(),
                    "Register 0 should be visible after finishProgrammingPhase!");
            for (int j = 1; j < Player.NO_REGISTERS; j++) {
                Assertions.assertFalse(board.getPlayer(i).getProgramField(j).isVisible(),
                        "Register " + j + " should be invisible!");
            }
        }
    }

    // ---------------------------------------------------------------
    //  Activation phase: executePrograms (continuous mode)
    // ---------------------------------------------------------------

    /** executePrograms should execute all registers for all players continuously. */
    @Test
    void executeProgramsContinuous() {
        Board board = gameController.board;
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                p.getProgramField(j).setCard(new CommandCard(Command.FORWARD));
            }
        }

        gameController.finishProgrammingPhase();
        gameController.executePrograms();

        Assertions.assertEquals(Phase.PROGRAMMING, board.getPhase(),
                "Phase should return to PROGRAMMING after all registers!");
    }

    /** executeStep should execute only one step. */
    @Test
    void executeStepSingle() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setSpace(board.getSpace(0, 0));
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.FORWARD));

        for (int i = 1; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            p.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(Phase.ACTIVATION, board.getPhase());
        Assertions.assertTrue(board.isStepMode());
        Assertions.assertEquals(board.getPlayer(1), board.getCurrentPlayer());
    }

    // ---------------------------------------------------------------
    //  executeCommand coverage: all command types via activation
    // ---------------------------------------------------------------

    /** Execute FORWARD command via activation phase. */
    @Test
    void executeForwardViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(p0, board.getSpace(3, 4).getPlayer());
    }

    /** Execute FAST_FORWARD command via activation phase. */
    @Test
    void executeFastForwardViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setSpace(board.getSpace(3, 0));
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.FAST_FORWARD));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(p0, board.getSpace(3, 2).getPlayer());
    }

    /** Execute RIGHT command via activation phase. */
    @Test
    void executeRightViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.RIGHT));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(Heading.WEST, p0.getHeading());
    }

    /** Execute LEFT command via activation phase. */
    @Test
    void executeLeftViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.LEFT));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(Heading.EAST, p0.getHeading());
    }

    /** Execute BACK command via activation phase. */
    @Test
    void executeBackViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.BACK));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(p0, board.getSpace(3, 2).getPlayer());
    }

    /** Execute UTURN command via activation phase. */
    @Test
    void executeUTurnViaProgram() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(new CommandCard(Command.UTURN));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(Heading.NORTH, p0.getHeading());
    }

    /** Execute with a null card in register (should skip without error). */
    @Test
    void executeNullCard() {
        Board board = gameController.board;
        Player p0 = board.getPlayer(0);
        p0.setSpace(board.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p0.getProgramField(0).setCard(null);

        for (int i = 1; i < board.getPlayersNumber(); i++) {
            board.getPlayer(i).getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep();

        Assertions.assertEquals(p0, board.getSpace(3, 3).getPlayer(),
                "Player should not move with a null card!");
    }

    // ---------------------------------------------------------------
    //  Win during activation: checkpoint triggers FINISHED mid-execution
    // ---------------------------------------------------------------

    /** Win during executePrograms should stop execution immediately. */
    @Test
    void winDuringExecutePrograms() {
        Board board = gameController.board;

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                p.getProgramField(j).setCard(new CommandCard(Command.FORWARD));
            }
        }

        Checkpoint lastCp = new Checkpoint();
        lastCp.setNumber(1);
        lastCp.setLastCheckpoint(true);
        board.getSpace(0, 1).getActions().add(lastCp);

        gameController.finishProgrammingPhase();
        gameController.executePrograms();

        Assertions.assertEquals(Phase.FINISHED, board.getPhase(),
                "Phase should be FINISHED after winning!");
        Assertions.assertEquals(board.getPlayer(0), board.getWinner(),
                "Player 0 should be the winner!");
    }

    /** Win during executeStep (field actions at end of register). */
    @Test
    void winDuringExecuteStep() {
        Board board = gameController.board;

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            p.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        Checkpoint lastCp = new Checkpoint();
        lastCp.setNumber(1);
        lastCp.setLastCheckpoint(true);
        board.getSpace(0, 1).getActions().add(lastCp);

        gameController.finishProgrammingPhase();

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            if (board.getPhase() != Phase.FINISHED) {
                gameController.executeStep();
            }
        }

        Assertions.assertEquals(Phase.FINISHED, board.getPhase());
    }

    /** Win during executeCommandOption via field actions (continuous mode). */
    @Test
    void winDuringExecuteCommandOption() {
        Board smallBoard = new Board(8, 8);
        GameController gc = new GameController(smallBoard);
        Player p0 = new Player(smallBoard, null, "P0");
        Player p1 = new Player(smallBoard, null, "P1");
        smallBoard.addPlayer(p0);
        smallBoard.addPlayer(p1);
        p0.setSpace(smallBoard.getSpace(3, 3));
        p0.setHeading(Heading.SOUTH);
        p1.setSpace(smallBoard.getSpace(5, 5));
        p1.setHeading(Heading.SOUTH);
        smallBoard.setCurrentPlayer(p0);

        p0.getProgramField(0).setCard(new CommandCard(Command.LEFT_OR_RIGHT));
        p1.getProgramField(0).setCard(new CommandCard(Command.FORWARD));

        // Place winning checkpoint on P0's space (3,3)
        Checkpoint cp = new Checkpoint();
        cp.setNumber(1);
        cp.setLastCheckpoint(true);
        smallBoard.getSpace(3, 3).getActions().add(cp);

        gc.finishProgrammingPhase();
        // Use executePrograms (continuous mode) so executeCommandOption will continue
        gc.executePrograms();

        Assertions.assertEquals(Phase.PLAYER_INTERACTION, smallBoard.getPhase());

        // Choose LEFT — P0 turns left, stays at (3,3)
        // In continuous mode, after option, it continues: P1 moves, field actions trigger win
        gc.executeCommandOption(Command.LEFT);

        Assertions.assertEquals(Phase.FINISHED, smallBoard.getPhase(),
                "Phase should be FINISHED after checkpoint win via interactive card!");
    }

    /** executeCommandOption in continuous mode should continue execution. */
    @Test
    void executeCommandOptionContinuousMode() {
        Board board = gameController.board;
        Player p0 = board.getCurrentPlayer();
        p0.setHeading(Heading.SOUTH);

        p0.getProgramField(0).setCard(new CommandCard(Command.LEFT_OR_RIGHT));
        for (int j = 1; j < Player.NO_REGISTERS; j++) {
            p0.getProgramField(j).setCard(new CommandCard(Command.FORWARD));
        }
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                p.getProgramField(j).setCard(new CommandCard(Command.FORWARD));
            }
        }

        gameController.finishProgrammingPhase();
        gameController.executePrograms(); // continuous mode

        Assertions.assertEquals(Phase.PLAYER_INTERACTION, board.getPhase());

        gameController.executeCommandOption(Command.LEFT);

        Assertions.assertEquals(Phase.PROGRAMMING, board.getPhase(),
                "Phase should return to PROGRAMMING after all registers in continuous mode!");
    }

    /** executeCommandOption in step mode should NOT continue automatically. */
    @Test
    void executeCommandOptionStepMode() {
        Board board = gameController.board;
        Player p0 = board.getCurrentPlayer();
        p0.setHeading(Heading.SOUTH);

        p0.getProgramField(0).setCard(new CommandCard(Command.LEFT_OR_RIGHT));
        for (int i = 1; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            p.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();
        gameController.executeStep(); // step mode

        Assertions.assertEquals(Phase.PLAYER_INTERACTION, board.getPhase());

        gameController.executeCommandOption(Command.RIGHT);

        Assertions.assertEquals(Phase.ACTIVATION, board.getPhase());
    }

    // ---------------------------------------------------------------
    //  Field actions during activation
    // ---------------------------------------------------------------

    /** Conveyor belt should be triggered during register execution. */
    @Test
    void conveyorBeltDuringActivation() {
        Board board = gameController.board;

        // Place players so that (1,1) is not occupied after movement.
        // P0 at (0,0) heading SOUTH → moves to (0,1) where belt pushes to (1,1)
        // P1 at (2,0) heading SOUTH → moves to (2,1), not blocking belt target
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            // Use column offset of 2 for non-first players to avoid (1,1)
            int col = (i == 0) ? 0 : i + 1;
            p.setSpace(board.getSpace(col, 0));
            p.setHeading(Heading.SOUTH);
            p.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
        }

        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(0, 1).getActions().add(belt);

        gameController.finishProgrammingPhase();

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            gameController.executeStep();
        }

        Player p0 = board.getPlayer(0);
        Assertions.assertEquals(p0, board.getSpace(1, 1).getPlayer(),
                "P0 should be at (1,1) after conveyor belt push!");
    }

    // ---------------------------------------------------------------
    //  Edge cases: moveForward/moveBackward with player having null space
    // ---------------------------------------------------------------

    /** moveForward should do nothing if player has no space (null). */
    @Test
    void moveForwardNullSpace() {
        Board board = gameController.board;
        Player p = board.getPlayer(0);
        p.setSpace(null);

        gameController.moveForward(p);

        Assertions.assertNull(p.getSpace(), "Player should still have null space!");
    }

    /** moveBackward should do nothing if player has no space (null). */
    @Test
    void moveBackwardNullSpace() {
        Board board = gameController.board;
        Player p = board.getPlayer(0);
        p.setSpace(null);

        gameController.moveBackward(p);

        Assertions.assertNull(p.getSpace(), "Player should still have null space!");
    }

    // ---------------------------------------------------------------
    //  advanceToNextPlayer: advance to next register step
    // ---------------------------------------------------------------

    /** After all players execute a register, the next register should start. */
    @Test
    void advanceToNextRegister() {
        Board board = gameController.board;

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            p.getProgramField(0).setCard(new CommandCard(Command.FORWARD));
            p.getProgramField(1).setCard(new CommandCard(Command.FORWARD));
        }

        gameController.finishProgrammingPhase();

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            gameController.executeStep();
        }

        Assertions.assertEquals(1, board.getStep(), "Step should advance to 1!");
        Assertions.assertEquals(board.getPlayer(0), board.getCurrentPlayer(),
                "Current player should reset to Player 0 for new register!");
    }

    /** After all 5 registers, should go back to programming phase. */
    @Test
    void allRegistersCompletedReturnsToProgramming() {
        Board board = gameController.board;

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player p = board.getPlayer(i);
            p.setSpace(board.getSpace(i, 0));
            p.setHeading(Heading.SOUTH);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                p.getProgramField(j).setCard(new CommandCard(Command.FORWARD));
            }
        }

        gameController.finishProgrammingPhase();
        gameController.executePrograms();

        Assertions.assertEquals(Phase.PROGRAMMING, board.getPhase(),
                "Phase should be PROGRAMMING after all registers completed!");
    }

    // ---------------------------------------------------------------
    //  Non-interactive commands should not be interactive
    // ---------------------------------------------------------------

    @Test
    void forwardIsNotInteractive() {
        Assertions.assertFalse(Command.FORWARD.isInteractive());
        Assertions.assertTrue(Command.FORWARD.getOptions().isEmpty());
    }

    // ---------------------------------------------------------------
    //  Model tests for Board, Player, Space
    // ---------------------------------------------------------------

    /** Board constructor with name. */
    @Test
    void boardWithName() {
        Board b = new Board(5, 5, "TestBoard");
        Assertions.assertEquals("TestBoard", b.boardName);
        Assertions.assertEquals(5, b.width);
        Assertions.assertEquals(5, b.height);
    }

    /** Board getSpace returns null for out-of-bounds. */
    @Test
    void boardGetSpaceOutOfBounds() {
        Board b = new Board(5, 5);
        Assertions.assertNull(b.getSpace(-1, 0));
        Assertions.assertNull(b.getSpace(0, -1));
        Assertions.assertNull(b.getSpace(5, 0));
        Assertions.assertNull(b.getSpace(0, 5));
    }

    /** Board getPlayer returns null for out-of-bounds index. */
    @Test
    void boardGetPlayerOutOfBounds() {
        Board b = new Board(5, 5);
        Assertions.assertNull(b.getPlayer(-1));
        Assertions.assertNull(b.getPlayer(0));
    }

    /** Player checkpoints getter/setter. */
    @Test
    void playerCheckpoints() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "TestPlayer");
        Assertions.assertEquals(0, p.getCheckpoints());
        p.setCheckpoints(3);
        Assertions.assertEquals(3, p.getCheckpoints());
    }

    /** Board winner getter/setter. */
    @Test
    void boardWinner() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "TestPlayer");
        b.addPlayer(p);
        Assertions.assertNull(b.getWinner());
        b.setWinner(p);
        Assertions.assertEquals(p, b.getWinner());
    }

    /** Board totalCheckpoints getter/setter. */
    @Test
    void boardTotalCheckpoints() {
        Board b = new Board(5, 5);
        Assertions.assertEquals(0, b.getTotalCheckpoints());
        b.setTotalCheckpoints(5);
        Assertions.assertEquals(5, b.getTotalCheckpoints());
    }

    /** Board status message when game is finished. */
    @Test
    void boardStatusMessageFinished() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "Winner");
        b.addPlayer(p);
        b.setCurrentPlayer(p);
        b.setWinner(p);
        b.setPhase(Phase.FINISHED);
        String msg = b.getStatusMessage();
        Assertions.assertTrue(msg.contains("Winner"),
                "Status message should contain winner name!");
        Assertions.assertTrue(msg.contains("won"),
                "Status message should say 'won'!");
    }

    /** Board status message during normal play. */
    @Test
    void boardStatusMessageNormal() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "Player1");
        b.addPlayer(p);
        b.setCurrentPlayer(p);
        String msg = b.getStatusMessage();
        Assertions.assertTrue(msg.contains("Phase"),
                "Status message should contain 'Phase'!");
        Assertions.assertTrue(msg.contains("Player1"),
                "Status message should contain player name!");
    }

    /** Board getNeighbour wraps around edges. */
    @Test
    void boardGetNeighbourWrapsAround() {
        Board b = new Board(8, 8);
        Space neighbour = b.getNeighbour(b.getSpace(0, 0), Heading.NORTH);
        Assertions.assertNotNull(neighbour);
        Assertions.assertEquals(0, neighbour.x);
        Assertions.assertEquals(7, neighbour.y);

        neighbour = b.getNeighbour(b.getSpace(0, 0), Heading.WEST);
        Assertions.assertNotNull(neighbour);
        Assertions.assertEquals(7, neighbour.x);
        Assertions.assertEquals(0, neighbour.y);
    }

    /** Heading next and prev should cycle correctly. */
    @Test
    void headingNextPrev() {
        Assertions.assertEquals(Heading.WEST, Heading.SOUTH.next());
        Assertions.assertEquals(Heading.NORTH, Heading.WEST.next());
        Assertions.assertEquals(Heading.EAST, Heading.NORTH.next());
        Assertions.assertEquals(Heading.SOUTH, Heading.EAST.next());

        Assertions.assertEquals(Heading.EAST, Heading.SOUTH.prev());
        Assertions.assertEquals(Heading.SOUTH, Heading.WEST.prev());
        Assertions.assertEquals(Heading.WEST, Heading.NORTH.prev());
        Assertions.assertEquals(Heading.NORTH, Heading.EAST.prev());
    }

    /** CommandCard getName returns display name. */
    @Test
    void commandCardGetName() {
        CommandCard card = new CommandCard(Command.FORWARD);
        Assertions.assertEquals("Fwd", card.getName());
    }

    /** Click-to-move for all four adjacent directions. */
    @Test
    void clickToMoveAllDirections() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
        current.setSpace(board.getSpace(4, 4));

        // Click NORTH (4,3)
        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 3));
        Assertions.assertEquals(current, board.getSpace(4, 3).getPlayer());

        // Click EAST (5,3)
        board.setCurrentPlayer(current);
        gameController.moveCurrentPlayerToSpace(board.getSpace(5, 3));
        Assertions.assertEquals(current, board.getSpace(5, 3).getPlayer());

        // Click WEST (4,3)
        board.setCurrentPlayer(current);
        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 3));
        Assertions.assertEquals(current, board.getSpace(4, 3).getPlayer());

        // Click SOUTH (4,4)
        board.setCurrentPlayer(current);
        gameController.moveCurrentPlayerToSpace(board.getSpace(4, 4));
        Assertions.assertEquals(current, board.getSpace(4, 4).getPlayer());
    }

    /** Board gameId setter. */
    @Test
    void boardGameId() {
        Board b = new Board(5, 5);
        Assertions.assertNull(b.getGameId());
        b.setGameId(42);
        Assertions.assertEquals(42, b.getGameId());
        b.setGameId(42);
        Assertions.assertEquals(42, b.getGameId());
        Assertions.assertThrows(IllegalStateException.class, () -> b.setGameId(99));
    }

    /** Board getPlayerNumber for player not on this board. */
    @Test
    void boardGetPlayerNumberWrongBoard() {
        Board b1 = new Board(5, 5);
        Board b2 = new Board(5, 5);
        Player p = new Player(b2, "red", "WrongBoard");
        b2.addPlayer(p);
        Assertions.assertEquals(-1, b1.getPlayerNumber(p));
    }

    /** Player setName and getColor. */
    @Test
    void playerNameAndColor() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "Alice");
        Assertions.assertEquals("Alice", p.getName());
        Assertions.assertEquals("red", p.getColor());
        p.setName("Bob");
        Assertions.assertEquals("Bob", p.getName());
        p.setColor("blue");
        Assertions.assertEquals("blue", p.getColor());
    }

    /** Player setSpace to null. */
    @Test
    void playerSetSpaceNull() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "Test");
        p.setSpace(b.getSpace(0, 0));
        Assertions.assertEquals(p, b.getSpace(0, 0).getPlayer());
        p.setSpace(null);
        Assertions.assertNull(p.getSpace());
    }

    /** Space getActions and getWalls return modifiable lists. */
    @Test
    void spaceActionsAndWalls() {
        Board b = new Board(5, 5);
        Space s = b.getSpace(0, 0);
        Assertions.assertNotNull(s.getActions());
        Assertions.assertTrue(s.getActions().isEmpty());
        Assertions.assertNotNull(s.getWalls());
        Assertions.assertTrue(s.getWalls().isEmpty());
    }

    /** CommandCardField visibility. */
    @Test
    void commandCardFieldVisibility() {
        Board b = new Board(5, 5);
        Player p = new Player(b, "red", "Test");
        CommandCardField field = p.getProgramField(0);
        Assertions.assertTrue(field.isVisible());
        field.setVisible(false);
        Assertions.assertFalse(field.isVisible());
        field.setVisible(true);
        Assertions.assertTrue(field.isVisible());
    }

    /** executeCommandOption should do nothing when phase is not PLAYER_INTERACTION. */
    @Test
    void executeCommandOptionWrongPhase() {
        Board board = gameController.board;
        Player p0 = board.getCurrentPlayer();
        p0.setHeading(Heading.SOUTH);

        // Phase is INITIALISATION — executeCommandOption should be a no-op
        gameController.executeCommandOption(Command.LEFT);

        Assertions.assertEquals(Heading.SOUTH, p0.getHeading(),
                "Heading should not change when executeCommandOption called in wrong phase!");
    }

    /** Player's board must match to execute command. */
    @Test
    void executeCommandPlayerBoardMismatch() {
        Board otherBoard = new Board(5, 5);
        Player foreignPlayer = new Player(otherBoard, "red", "Foreign");
        otherBoard.addPlayer(foreignPlayer);
        foreignPlayer.setSpace(otherBoard.getSpace(0, 0));
        foreignPlayer.setHeading(Heading.SOUTH);

        // moveForward on a player whose board differs — should not crash
        // but internally executeCommand guards with player.board == board
        gameController.moveForward(foreignPlayer);

        // The player should still be on (0,0) of otherBoard
        Assertions.assertEquals(foreignPlayer, otherBoard.getSpace(0, 0).getPlayer());
    }
}


