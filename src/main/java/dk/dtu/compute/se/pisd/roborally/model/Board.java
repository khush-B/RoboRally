/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dk.dtu.compute.se.pisd.roborally.model.Phase.INITIALISATION;

/**
 * This represents a RoboRally game board. Which gives access to
 * all the information of current state of the games. Note that
 * the terms board and game are used almost interchangeably.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Board extends Subject {

    public final int width;

    public final int height;

    public final String boardName;

    private Integer gameId;

    private final Space[][] spaces;

    private final List<Player> players = new ArrayList<>();

    private Player current;

    private Phase phase = INITIALISATION;

    private int step = 0;

    private boolean stepMode;

    /**
     * Counts the total number of moves (clicks) made during the game.
     * Incremented each time a player is successfully moved to a new space.
     */
    private int moveCounter = 0;

    /** The player who has won the game, or null if no winner yet. */
    private Player winner = null;

    /** The total number of checkpoints on this board (set during board setup). */
    private int totalCheckpoints = 0;

    public Board(int width, int height, @NotNull String boardName) {
        this.boardName = boardName;
        this.width = width;
        this.height = height;
        spaces = new Space[width][height];
        for (int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Space space = new Space(this, x, y);
                spaces[x][y] = space;
            }
        }
        this.stepMode = false;
    }

    public Board(int width, int height) {
        this(width, height, "defaultboard");
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        if (this.gameId == null) {
            this.gameId = gameId;
        } else {
            if (!this.gameId.equals(gameId)) {
                throw new IllegalStateException("A game with a set id may not be assigned a new id!");
            }
        }
    }

    public Space getSpace(int x, int y) {
        if (x >= 0 && x < width &&
                y >= 0 && y < height) {
            return spaces[x][y];
        } else {
            return null;
        }
    }

    public int getPlayersNumber() {
        return players.size();
    }

    public void addPlayer(@NotNull Player player) {
        if (player.board == this && !players.contains(player)) {
            players.add(player);
            notifyChange();
        }
    }

    public Player getPlayer(int i) {
        if (i >= 0 && i < players.size()) {
            return players.get(i);
        } else {
            return null;
        }
    }

    public Player getCurrentPlayer() {
        return current;
    }

    public void setCurrentPlayer(Player player) {
        if (player != this.current && players.contains(player)) {
            this.current = player;
            notifyChange();
        }
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        if (phase != this.phase) {
            this.phase = phase;
            notifyChange();
        }
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        if (step != this.step) {
            this.step = step;
            notifyChange();
        }
    }

    public boolean isStepMode() {
        return stepMode;
    }

    public void setStepMode(boolean stepMode) {
        if (stepMode != this.stepMode) {
            this.stepMode = stepMode;
            notifyChange();
        }
    }

    /**
     * Returns the current move counter value.
     *
     * @return the current move count
     */
    public int getCounter() {
        return moveCounter;
    }

    /**
     * Sets the move counter and notifies observers.
     *
     * @param counter the new move count value
     */
    public void setCounter(int counter) {
        if (counter != this.moveCounter) {
            this.moveCounter = counter;
            notifyChange();
        }
    }

    /**
     * Returns the player who has won the game.
     *
     * @return the winning player, or null if no winner yet
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Sets the winner of the game and notifies observers.
     *
     * @param winner the player who won
     */
    public void setWinner(Player winner) {
        this.winner = winner;
        notifyChange();
    }

    /**
     * Returns the total number of checkpoints on this board.
     *
     * @return the total checkpoint count
     */
    public int getTotalCheckpoints() {
        return totalCheckpoints;
    }

    /**
     * Sets the total number of checkpoints on this board.
     *
     * @param totalCheckpoints the total number of checkpoints
     */
    public void setTotalCheckpoints(int totalCheckpoints) {
        this.totalCheckpoints = totalCheckpoints;
    }

    public int getPlayerNumber(@NotNull Player player) {
        if (player.board == this) {
            return players.indexOf(player);
        } else {
            return -1;
        }
    }

    /**
     * Returns the neighbour of the given space of the board in the given heading.
     * The neighbour is returned only, if it can be reached from the given space
     * (no walls or obstacles in either of the involved spaces); otherwise,
     * null will be returned.
     *
     * @param space the space for which the neighbour should be computed
     * @param heading the heading of the neighbour
     * @return the space in the given direction; null if there is no (reachable) neighbour
     */
    public Space getNeighbour(@NotNull Space space, @NotNull Heading heading) {
        // Check for a wall on the current space blocking exit in the given heading
        if (space.getWalls().contains(heading)) {
            return null;
        }

        int x = space.x;
        int y = space.y;
        switch (heading) {
            case SOUTH:
                y = (y + 1) % height;
                break;
            case WEST:
                x = (x + width - 1) % width;
                break;
            case NORTH:
                y = (y + height - 1) % height;
                break;
            case EAST:
                x = (x + 1) % width;
                break;
        }

        Space neighbour = getSpace(x, y);

        // Check for a wall on the target space blocking entry from the opposite direction
        if (neighbour != null) {
            Heading opposite = heading.next().next(); // opposite direction
            if (neighbour.getWalls().contains(opposite)) {
                return null;
            }
        }

        return neighbour;
    }

    /**
     * Returns a status message string for display in the GUI status bar.
     * Shows the current phase, current player, and step number.
     *
     * @return the status message string
     */
    public String getStatusMessage() {
        if (getPhase() == Phase.FINISHED && winner != null) {
            return winner.getName() + " has won the game!";
        }
        return "Phase: " + getPhase().name() +
               ", Player: " + getCurrentPlayer().getName() +
               ", Step: " + getStep();
    }

}
