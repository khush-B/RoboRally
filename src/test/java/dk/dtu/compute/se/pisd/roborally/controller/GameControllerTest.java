package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the GameController covering Assignments 6a, 6c, and 6d.
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
        // Player 0 at (0,0) heading SOUTH
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
        // Place player at (3,3) heading SOUTH — backward is NORTH → (3,2)
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

        // Put a SOUTH wall on (2,2)
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

        // Put a NORTH wall on (2,3) — blocks entry from the north
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

        // NORTH wall on (3,3) blocks backward (=NORTH) exit
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
        // Wall blocks the pushed robot from moving further south
        board.getSpace(3, 4).getWalls().add(Heading.SOUTH);

        gameController.moveForward(pusher);

        Assertions.assertEquals(pusher, board.getSpace(3, 3).getPlayer(),
                "Pusher should NOT have moved — push blocked by wall!");
        Assertions.assertEquals(pushed, board.getSpace(3, 4).getPlayer(),
                "Pushed robot should NOT have moved!");
    }

    // ---------------------------------------------------------------
    //  Assignment 6d: field actions
    // ---------------------------------------------------------------

    /** ConveyorBelt should move a player one space in the belt's direction. */
    @Test
    void conveyorBeltMovesPlayer() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));

        // Add east-pointing conveyor belt to (4,4)
        ConveyorBelt belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        board.getSpace(4, 4).getActions().add(belt);

        belt.doAction(gameController, board.getSpace(4, 4));

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

        belt.doAction(gameController, board.getSpace(4, 4));

        Assertions.assertEquals(player, board.getSpace(4, 4).getPlayer(),
                "Player should NOT move — belt blocked by wall!");
    }

    /** Checkpoint should be collected only in order. */
    @Test
    void checkpointCollectedInOrder() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        player.setSpace(board.getSpace(4, 4));
        Assertions.assertEquals(0, player.getCheckpoints());

        // Checkpoint 1
        Checkpoint cp1 = new Checkpoint();
        cp1.setNumber(1);
        board.getSpace(4, 4).getActions().add(cp1);

        cp1.doAction(gameController, board.getSpace(4, 4));
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

        // Try checkpoint 2 directly
        Checkpoint cp2 = new Checkpoint();
        cp2.setNumber(2);
        board.getSpace(4, 4).getActions().add(cp2);

        cp2.doAction(gameController, board.getSpace(4, 4));
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

        // Collect checkpoint 1
        player.setSpace(board.getSpace(2, 2));
        cp1.doAction(gameController, board.getSpace(2, 2));
        Assertions.assertEquals(1, player.getCheckpoints());

        // Now collect checkpoint 2
        player.setSpace(board.getSpace(3, 3));
        cp2.doAction(gameController, board.getSpace(3, 3));
        Assertions.assertEquals(2, player.getCheckpoints());
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

        // Add a SOUTH wall on (4,4)
        board.getSpace(4, 4).getWalls().add(Heading.SOUTH);

        // Click on the adjacent space to the south
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

        // Click on (4,5) which is adjacent and occupied by 'other'
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
        // Wall on (4,5) SOUTH blocks the push
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
        player.setCheckpoints(2); // already collected 1 and 2

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

        // Set up: put an interactive card in register 0 and start activation
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
}