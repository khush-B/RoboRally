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

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

/**
 * The main controller for the RoboRally game logic. Handles programming
 * and activation phases, command execution, movement (with wall checks
 * and robot pushing), and field action execution.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class GameController {

    /** The board (game state) controlled by this controller. */
    final public Board board;

    /**
     * Creates a new GameController for the given board.
     *
     * @param board the board to control
     */
    public GameController(@NotNull Board board) {
        this.board = board;
    }

    // ---------------------------------------------------------------
    //  Assignment 6a: click-to-move (for manual testing)
    // ---------------------------------------------------------------

    /**
     * Moves the current player to the given space when the user clicks on it.
     * <p>
     * For <b>adjacent spaces</b> (1 step away): walls are checked and robots
     * are pushed, exactly as during normal command execution. If a wall
     * blocks the move or a push chain cannot be completed, nothing happens.
     * <p>
     * For <b>non-adjacent spaces</b>: the player is teleported there only
     * if the space is empty (original Assignment 6a behaviour).
     * <p>
     * After a successful move the turn advances to the next player and the
     * board's move counter is incremented by one.
     *
     * @param space the target space to move the current player to
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space) {
        Player currentPlayer = board.getCurrentPlayer();
        if (currentPlayer == null) return;
        Space currentSpace = currentPlayer.getSpace();

        if (currentSpace != null) {
            // Check if the target space is adjacent (1 step in any direction)
            for (Heading heading : Heading.values()) {
                // Compute raw neighbour coordinates (ignoring walls)
                int nx = currentSpace.x;
                int ny = currentSpace.y;
                switch (heading) {
                    case SOUTH: ny = (ny + 1) % board.height; break;
                    case NORTH: ny = (ny + board.height - 1) % board.height; break;
                    case EAST:  nx = (nx + 1) % board.width; break;
                    case WEST:  nx = (nx + board.width - 1) % board.width; break;
                }
                if (space.x == nx && space.y == ny) {
                    // Target IS adjacent — check walls via getNeighbour
                    Space wallChecked = board.getNeighbour(currentSpace, heading);
                    if (wallChecked == null) {
                        return; // wall blocks this move
                    }
                    // Push robots if the target space is occupied
                    Player targetPlayer = space.getPlayer();
                    if (targetPlayer != null) {
                        pushRobot(targetPlayer, heading);
                        if (space.getPlayer() != null) {
                            return; // push chain blocked
                        }
                    }
                    currentPlayer.setSpace(space);
                    advanceTurn(currentPlayer);
                    return;
                }
            }
        }

        // Non-adjacent space — teleport only if empty (6a backward compatibility)
        if (space.getPlayer() == null) {
            currentPlayer.setSpace(space);
            advanceTurn(currentPlayer);
        }
    }

    /**
     * Advances the turn to the next player and increments the move counter.
     *
     * @param currentPlayer the player who just moved
     */
    private void advanceTurn(@NotNull Player currentPlayer) {
        int nextPlayerIndex = (board.getPlayerNumber(currentPlayer) + 1)
                % board.getPlayersNumber();
        board.setCurrentPlayer(board.getPlayer(nextPlayerIndex));
        board.setCounter(board.getCounter() + 1);
    }

    // ---------------------------------------------------------------
    //  Programming phase
    // ---------------------------------------------------------------

    /**
     * Starts the programming phase: resets the step, deals random
     * command cards to all players' hands, and clears their programs.
     */
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    /**
     * Generates a random {@link CommandCard} from all available commands.
     *
     * @return a randomly chosen command card
     */
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * Finishes the programming phase: hides all program fields, then
     * makes the first register visible, and transitions to the
     * activation phase starting with player 0 at step 0.
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    /**
     * Makes the program field of the given register visible for all players.
     *
     * @param register the register index (0-based) to reveal
     */
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    /**
     * Hides all program fields for all players.
     */
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    // ---------------------------------------------------------------
    //  Activation phase: executing programs
    // ---------------------------------------------------------------

    /**
     * Executes all remaining registers without stopping (continuous mode).
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * Executes only the current register step (step-by-step mode).
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * Continues executing register steps. In step mode, only one step
     * is executed; in continuous mode, all remaining steps are executed.
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    /**
     * Executes the next step of the activation phase: runs the command
     * card in the current player's current register, advances to the
     * next player, and after all players have executed a register,
     * triggers field actions and moves to the next register.
     */
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    executeCommand(currentPlayer, command);
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    // All players have executed this register — run field actions
                    executeFieldActions();

                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                assert false;
            }
        } else {
            assert false;
        }
    }

    /**
     * Executes all field actions on all spaces occupied by players.
     * This is called once after all players have executed their command
     * card for a given register.
     */
    private void executeFieldActions() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            Space space = player.getSpace();
            if (space != null) {
                for (FieldAction action : space.getActions()) {
                    action.doAction(this, space);
                }
            }
        }
    }

    /**
     * Dispatches a command to the appropriate method for execution.
     *
     * @param player  the player whose robot executes the command
     * @param command the command to execute
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {
            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                case BACK:
                    this.moveBackward(player);
                    break;
                case UTURN:
                    this.uTurn(player);
                    break;
                default:
                    // DO NOTHING
            }
        }
    }

    // ---------------------------------------------------------------
    //  Movement commands (A6c + A6d pushing)
    // ---------------------------------------------------------------

    /**
     * Moves the given player one space forward in their current heading
     * direction. If another robot is on the target space, it is pushed
     * recursively. The move is blocked if a wall prevents it.
     *
     * @param player the player to move forward
     */
    public void moveForward(@NotNull Player player) {
        Space current = player.getSpace();
        if (current != null) {
            Heading heading = player.getHeading();
            Space target = board.getNeighbour(current, heading);
            if (target != null) {
                // Push any robot on the target space
                Player targetPlayer = target.getPlayer();
                if (targetPlayer != null) {
                    pushRobot(targetPlayer, heading);
                    // If push failed (robot is still there), abort
                    if (target.getPlayer() != null) {
                        return;
                    }
                }
                player.setSpace(target);
            }
        }
    }

    /**
     * Moves the given player two spaces forward (fast forward).
     * Each individual step checks for walls and pushes robots.
     *
     * @param player the player to move two spaces forward
     */
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
    }

    /**
     * Turns the given player 90° clockwise (to the right).
     *
     * @param player the player to turn right
     */
    public void turnRight(@NotNull Player player) {
        player.setHeading(player.getHeading().next());
    }

    /**
     * Turns the given player 90° counter-clockwise (to the left).
     *
     * @param player the player to turn left
     */
    public void turnLeft(@NotNull Player player) {
        player.setHeading(player.getHeading().prev());
    }

    /**
     * Moves the given player one space backward (opposite of their
     * current heading) without changing their heading direction.
     * Pushes robots in the backward direction if necessary.
     *
     * @param player the player to move backward
     */
    public void moveBackward(@NotNull Player player) {
        Space current = player.getSpace();
        if (current != null) {
            // Move in opposite direction without changing heading
            Heading backward = player.getHeading().next().next();
            Space target = board.getNeighbour(current, backward);
            if (target != null) {
                Player targetPlayer = target.getPlayer();
                if (targetPlayer != null) {
                    pushRobot(targetPlayer, backward);
                    if (target.getPlayer() != null) {
                        return;
                    }
                }
                player.setSpace(target);
            }
        }
    }

    /**
     * Makes the given player perform a U-turn (180° rotation) without
     * moving from their current space.
     *
     * @param player the player to make a U-turn
     */
    public void uTurn(@NotNull Player player) {
        player.setHeading(player.getHeading().next().next());
    }

    /**
     * Recursively pushes a robot one space in the given heading direction.
     * If the target space is occupied by another robot, that robot is
     * pushed first. The push fails silently if blocked by a wall.
     *
     * @param player  the player/robot to push
     * @param heading the direction to push the robot
     */
    private void pushRobot(@NotNull Player player, @NotNull Heading heading) {
        Space current = player.getSpace();
        if (current != null) {
            Space target = board.getNeighbour(current, heading);
            if (target != null) {
                Player targetPlayer = target.getPlayer();
                if (targetPlayer != null) {
                    // Recursively push the next robot
                    pushRobot(targetPlayer, heading);
                    if (target.getPlayer() != null) {
                        return; // push chain blocked
                    }
                }
                player.setSpace(target);
            }
        }
    }

    /**
     * A method called when no corresponding controller operation is
     * implemented yet. This should eventually be removed.
     */
    public void notImplemented() {
        assert false;
    }

}
