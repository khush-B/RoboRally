package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.util.Arrays;
import java.util.List;

/**
 * A factory for creating boards. The factory itself is implemented as a
 * singleton. It provides a list of available board names and can create
 * a fully configured {@link Board} for any of those names.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class BoardFactory {

    /** The name of the first board layout. */
    private static final String BOARD_1 = "Classic Arena";
    /** The name of the second board layout. */
    private static final String BOARD_2 = "Sprint Track";

    /** The single instance of this class, which is lazily instantiated on demand. */
    private static BoardFactory instance = null;

    /**
     * Constructor for BoardFactory. It is private in order to make the factory a singleton.
     */
    private BoardFactory() {
    }

    /**
     * Returns the single instance of this factory. The instance is lazily
     * instantiated when requested for the first time.
     *
     * @return the single instance of the BoardFactory
     */
    public static BoardFactory getInstance() {
        if (instance == null) {
            instance = new BoardFactory();
        }
        return instance;
    }

    /**
     * Returns a list of the names of all available boards that can be
     * created by this factory. The method {@link #createBoard(String)}
     * can create a board for any of the names in this list.
     *
     * @return an unmodifiable list of available board names
     */
    public List<String> getNames() {
        return Arrays.asList(BOARD_1, BOARD_2);
    }

    /**
     * Creates a new board for the given board name. The board will be
     * pre-configured with walls, conveyor belts, and checkpoints that
     * correspond to the chosen layout. If the name is {@code null} or
     * not recognised, the default board ({@value #BOARD_1}) is created.
     *
     * @param name the name of the board layout to create
     * @return a fully configured {@link Board}
     */
    public Board createBoard(String name) {
        if (name == null) {
            name = BOARD_1;
        }

        Board board;
        switch (name) {
            case BOARD_2:
                board = createSprintTrack();
                break;
            case BOARD_1:
            default:
                board = createClassicArena();
                break;
        }
        return board;
    }

    /**
     * Creates the "Classic Arena" board — an 8×8 board with walls on
     * multiple sides, conveyor belts forming a path, and three checkpoints.
     *
     * @return the configured Classic Arena board
     */
    private Board createClassicArena() {
        Board board = new Board(8, 8, BOARD_1);
        Space space;
        ConveyorBelt belt;
        Checkpoint cp;

        // --- Walls ---
        space = board.getSpace(0, 0);
        space.getWalls().add(Heading.SOUTH);

        space = board.getSpace(1, 0);
        space.getWalls().add(Heading.NORTH);

        space = board.getSpace(1, 1);
        space.getWalls().add(Heading.WEST);

        space = board.getSpace(5, 5);
        space.getWalls().add(Heading.SOUTH);

        space = board.getSpace(3, 3);
        space.getWalls().add(Heading.EAST);
        space.getWalls().add(Heading.SOUTH);

        space = board.getSpace(6, 2);
        space.getWalls().add(Heading.WEST);

        // --- Conveyor Belts ---
        space = board.getSpace(0, 0);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        space.getActions().add(belt);

        space = board.getSpace(1, 0);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.EAST);
        space.getActions().add(belt);

        space = board.getSpace(2, 0);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.SOUTH);
        space.getActions().add(belt);

        space = board.getSpace(5, 5);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.WEST);
        space.getActions().add(belt);

        space = board.getSpace(6, 5);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.WEST);
        space.getActions().add(belt);

        space = board.getSpace(4, 7);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.NORTH);
        space.getActions().add(belt);

        // --- Checkpoints ---
        space = board.getSpace(3, 1);
        cp = new Checkpoint();
        cp.setNumber(1);
        space.getActions().add(cp);

        space = board.getSpace(5, 4);
        cp = new Checkpoint();
        cp.setNumber(2);
        space.getActions().add(cp);

        space = board.getSpace(7, 7);
        cp = new Checkpoint();
        cp.setNumber(3);
        space.getActions().add(cp);

        return board;
    }

    /**
     * Creates the "Sprint Track" board — a 10×8 board with a long
     * conveyor belt corridor, walls creating a maze-like path, and
     * three checkpoints spread across the track.
     *
     * @return the configured Sprint Track board
     */
    private Board createSprintTrack() {
        Board board = new Board(10, 8, BOARD_2);
        Space space;
        ConveyorBelt belt;
        Checkpoint cp;

        // --- Walls ---
        space = board.getSpace(2, 1);
        space.getWalls().add(Heading.SOUTH);

        space = board.getSpace(2, 2);
        space.getWalls().add(Heading.NORTH);

        space = board.getSpace(4, 3);
        space.getWalls().add(Heading.EAST);

        space = board.getSpace(7, 1);
        space.getWalls().add(Heading.WEST);
        space.getWalls().add(Heading.SOUTH);

        space = board.getSpace(5, 5);
        space.getWalls().add(Heading.NORTH);

        space = board.getSpace(8, 6);
        space.getWalls().add(Heading.WEST);

        space = board.getSpace(1, 6);
        space.getWalls().add(Heading.EAST);

        // --- Conveyor Belts (long east-bound corridor on row 0) ---
        for (int x = 0; x < 9; x++) {
            space = board.getSpace(x, 0);
            belt = new ConveyorBelt();
            belt.setHeading(Heading.EAST);
            space.getActions().add(belt);
        }

        // South-bound column on the right
        space = board.getSpace(9, 0);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.SOUTH);
        space.getActions().add(belt);

        space = board.getSpace(9, 1);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.SOUTH);
        space.getActions().add(belt);

        // West-bound corridor on row 4
        for (int x = 8; x >= 3; x--) {
            space = board.getSpace(x, 4);
            belt = new ConveyorBelt();
            belt.setHeading(Heading.WEST);
            space.getActions().add(belt);
        }

        // North-bound connector
        space = board.getSpace(3, 7);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.NORTH);
        space.getActions().add(belt);

        space = board.getSpace(3, 6);
        belt = new ConveyorBelt();
        belt.setHeading(Heading.NORTH);
        space.getActions().add(belt);

        // --- Checkpoints ---
        space = board.getSpace(4, 1);
        cp = new Checkpoint();
        cp.setNumber(1);
        space.getActions().add(cp);

        space = board.getSpace(7, 4);
        cp = new Checkpoint();
        cp.setNumber(2);
        space.getActions().add(cp);

        space = board.getSpace(1, 7);
        cp = new Checkpoint();
        cp.setNumber(3);
        space.getActions().add(cp);

        return board;
    }

}
