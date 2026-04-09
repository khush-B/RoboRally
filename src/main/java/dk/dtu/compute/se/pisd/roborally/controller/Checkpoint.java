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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a checkpoint on a space of the game board. Each checkpoint
 * has a unique number that indicates the order in which players must
 * reach the checkpoints to win the game. The last checkpoint on the
 * board triggers the win condition.
 *
 * @author Advanced Programming Group
 */
public class Checkpoint extends FieldAction {

    /** The number identifying this checkpoint (1-based ordering). */
    private int number;

    /** Whether this is the last checkpoint on the board (triggers win). */
    private boolean isLastCheckpoint = false;

    /**
     * Returns the number of this checkpoint.
     *
     * @return the checkpoint number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the number of this checkpoint.
     *
     * @param number the checkpoint number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Returns whether this is the last checkpoint on the board.
     *
     * @return true if this is the final checkpoint
     */
    public boolean isLastCheckpoint() {
        return isLastCheckpoint;
    }

    /**
     * Sets whether this is the last checkpoint on the board.
     *
     * @param lastCheckpoint true if this is the final checkpoint
     */
    public void setLastCheckpoint(boolean lastCheckpoint) {
        this.isLastCheckpoint = lastCheckpoint;
    }

    /**
     * Executes the checkpoint action: if the player on the given space
     * has already collected all lower-numbered checkpoints, then the
     * player's checkpoint count is incremented. If this is the last
     * checkpoint, the game ends and the player wins.
     *
     * @param gameController the game controller of the current game
     * @param space          the space that contains this checkpoint
     * @return true if the checkpoint was successfully collected
     */
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player != null) {
            if (player.getCheckpoints() == number - 1) {
                player.setCheckpoints(number);
                // If this is the last checkpoint, the player wins
                if (isLastCheckpoint) {
                    gameController.board.setWinner(player);
                    gameController.board.setPhase(Phase.FINISHED);
                }
                return true;
            }
        }
        return false;
    }

}

