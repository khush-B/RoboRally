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

import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents a conveyor belt on a space. When activated,
 * it moves any robot standing on it one space in the belt's heading
 * direction (if not blocked by a wall).
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class ConveyorBelt extends FieldAction {

    /** The direction this conveyor belt pushes robots. */
    private Heading heading;

    /**
     * Returns the heading direction of this conveyor belt.
     *
     * @return the heading of this belt
     */
    public Heading getHeading() {
        return heading;
    }

    /**
     * Sets the heading direction of this conveyor belt.
     *
     * @param heading the heading to set
     */
    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    /**
     * Executes the conveyor belt action: moves the player on the given
     * space one space in the belt's heading direction, provided no wall
     * blocks the movement and the target space is free.
     *
     * @param gameController the game controller of the current game
     * @param space          the space containing this conveyor belt
     * @return true if the player was successfully moved
     */
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player != null) {
            Space target = gameController.board.getNeighbour(space, heading);
            if (target != null && target.getPlayer() == null) {
                player.setSpace(target);
                return true;
            }
        }
        return false;
    }

}
