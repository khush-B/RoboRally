# RoboRally — Assignment 6a & 6b Documentation

**Course:** Advanced Programming (02324), Spring 2026  
**Authors:** Group Members  
**Date:** March 26, 2026

---

## Table of Contents

1. [Assignment 6a — Getting Started with RoboRally](#assignment-6a--getting-started-with-roborally)
   - [Requirements](#6a-requirements)
   - [Implementation Details](#6a-implementation-details)
   - [Files Modified](#6a-files-modified)
2. [Assignment 6b — Choosing and Showing Boards](#assignment-6b--choosing-and-showing-boards)
   - [Requirements](#6b-requirements)
   - [Implementation Details](#6b-implementation-details)
   - [Files Modified / Created](#6b-files-modified--created)
3. [RoboRally Game Rules Overview](#roborally-game-rules-overview)
4. [Project Architecture Overview](#project-architecture-overview)

---

## Assignment 6a — Getting Started with RoboRally

### 6a Requirements

The goal of Assignment 6a is to introduce basic player interaction with the board:

1. **Move on click:** When the user clicks on an empty space, the current player's robot moves to that space.
2. **Turn rotation:** After each successful move, the turn passes to the next player in the list (wrapping around to Player 0 after the last player).
3. **Move counter:** A counter tracks the total number of successful moves and is displayed in the GUI status bar.
4. **Status message:** The status line shows both the current player's name and the move count.
5. **Testing:** The existing JUnit test (`testV1`) must pass.

### 6a Implementation Details

#### 1. `GameController.moveCurrentPlayerToSpace(Space space)`

This is the core method for Assignment 6a. The logic is:

```
if the target space has no player on it:
    1. Get the current player from the board
    2. Move that player to the target space via player.setSpace(space)
    3. Calculate the next player index: (currentIndex + 1) % totalPlayers
    4. Set the next player as the current player on the board
    5. Increment the board's move counter by 1
```

**Key design decisions:**
- The `setSpace()` method on `Player` automatically handles removing the player from their old space and placing them on the new space (bidirectional relationship management).
- The guard condition `space.getPlayer() == null` ensures we only move to empty spaces.
- Modulo arithmetic `% board.getPlayersNumber()` ensures the turn wraps from the last player back to the first.

#### 2. `Board` — Move Counter

Three additions were made to the `Board` class:

| Element | Description |
|---------|-------------|
| `private int moveCounter = 0` | Field that stores the total number of moves |
| `getCounter()` | Returns the current counter value |
| `setCounter(int counter)` | Updates the counter and calls `notifyChange()` to trigger GUI refresh |

**Why `notifyChange()`?**  
The `Board` class extends `Subject` (Observer pattern). Calling `notifyChange()` tells all registered observers (e.g. the `BoardView`) that the model has changed, so the status label on the GUI is updated automatically.

#### 3. `Board.getStatusMessage()`

Updated to return:
```java
"Player = " + getCurrentPlayer().getName() + ", Moves = " + moveCounter
```

This string is picked up by `BoardView.updateView()` which sets it as the text of the `statusLabel`.

### 6a Files Modified

| File | Change |
|------|--------|
| `model/Board.java` | Added `moveCounter` field, `getCounter()`, `setCounter()`, updated `getStatusMessage()` |
| `controller/GameController.java` | Implemented `moveCurrentPlayerToSpace()` |
| `pom.xml` | Changed `jdk.version` from `25` to `21` to match the installed JDK |

---

## Assignment 6b — Choosing and Showing Boards

### 6b Requirements

1. **Board selection:** When starting a new game, the user is presented with a dialog to choose between at least two different boards.
2. **BoardFactory:** The singleton `BoardFactory` must provide:
   - `getNames()` — returns a list of all available board names.
   - `createBoard(String name)` — creates and returns a fully configured board for any valid name.
3. **Board features:** Each board must contain:
   - **Walls** on various sides of spaces
   - **Conveyor belts** facing different directions
   - **Checkpoints** with their numbers
4. **Checkpoint class:** A new `Checkpoint` class that extends `FieldAction` (similar to `ConveyorBelt`).
5. **SpaceView rendering:** The `updateView()` method in `SpaceView` must visually draw walls, conveyor belts, and checkpoints.
6. **Assignment 6a compatibility:** Clicking empty spaces to move players must still work.

### 6b Implementation Details

#### 1. `Checkpoint` Class (New File)

```
Checkpoint extends FieldAction
├── int number        — the checkpoint number (1, 2, 3, ...)
├── getNumber()       — returns the checkpoint number
├── setNumber(int)    — sets the checkpoint number
└── doAction(...)     — placeholder for future A6d implementation
```

Modelled after `ConveyorBelt`: it is a `FieldAction` placed in the `actions` list of a `Space`. The `number` field indicates the order in which players must reach checkpoints.

#### 2. `BoardFactory` — Two Board Layouts

| Board Name | Dimensions | Description |
|------------|-----------|-------------|
| **Classic Arena** | 8 × 8 | Walls on 6 spaces, 6 conveyor belts forming paths, 3 checkpoints |
| **Sprint Track** | 10 × 8 | Long east-bound conveyor corridor, maze-like walls, west-bound return corridor, 3 checkpoints |

**`getNames()`** returns `["Classic Arena", "Sprint Track"]`.

**`createBoard(String name)`** uses a `switch` statement to delegate to private helper methods:
- `createClassicArena()` — builds the 8×8 board
- `createSprintTrack()` — builds the 10×8 board
- If `name` is `null`, defaults to "Classic Arena" (defensive programming).

Each helper method:
1. Creates a `Board` with the appropriate dimensions and name
2. Adds walls to specific spaces via `space.getWalls().add(Heading.XXX)`
3. Creates `ConveyorBelt` objects with specific headings and adds them via `space.getActions().add(belt)`
4. Creates `Checkpoint` objects with numbered ordering and adds them via `space.getActions().add(cp)`

#### 3. `AppController.newGame()` — Board Selection Dialog

After the player-count dialog, a second `ChoiceDialog<String>` is shown:

```
1. Get the BoardFactory singleton
2. Get the list of available board names via factory.getNames()
3. Show a ChoiceDialog with these names
4. Create the selected board via factory.createBoard(selectedName)
5. Continue with the existing game setup (adding players, starting programming phase)
```

If the user cancels the board dialog, the default board is created (null is passed to `createBoard`).

#### 4. `SpaceView.updateView()` — Visual Rendering

The `updateView()` method now calls three helper methods in order (back to front):

| Method | What it draws | Visual |
|--------|--------------|--------|
| `drawFieldActions()` | Iterates over `space.getActions()` and delegates to type-specific draw methods | — |
| `drawConveyorBelt()` | A light-green arrow rotated to match the belt's heading | ▲ green arrow |
| `drawCheckpoint()` | A yellow circle with a bold number in the centre | ① yellow circle |
| `drawWalls()` | A thick red line along the corresponding edge (N/S/E/W) using Canvas | — red line |
| `updatePlayer()` | The player's coloured directional arrow (unchanged from 6a) | ▲ player colour |

**Rendering order:** Field actions → Walls → Player. This ensures the player arrow is always on top and walls are visible above conveyor belt arrows.

**Wall drawing** uses a `Canvas` with `GraphicsContext`:
- **SOUTH** wall → horizontal line at y = SPACE_HEIGHT - 1
- **NORTH** wall → horizontal line at y = 1
- **WEST** wall → vertical line at x = 1
- **EAST** wall → vertical line at x = SPACE_WIDTH - 1

### 6b Files Modified / Created

| File | Change |
|------|--------|
| `controller/Checkpoint.java` | **NEW** — Checkpoint field action with number attribute |
| `controller/BoardFactory.java` | Added `getNames()`, rewrote `createBoard()` with two board layouts |
| `controller/AppController.java` | Added board selection `ChoiceDialog` in `newGame()` |
| `view/SpaceView.java` | Implemented `drawWalls()`, `drawConveyorBelt()`, `drawCheckpoint()` in `updateView()` |

---

## RoboRally Game Rules Overview

RoboRally is a board game where players program robots to navigate a factory floor:

| Concept | Description |
|---------|-------------|
| **Board** | A grid of spaces representing the factory floor |
| **Players / Robots** | Each player controls a robot placed on the board |
| **Heading** | Robots face one of four directions: NORTH, SOUTH, EAST, WEST |
| **Programming** | Players play command cards (Move Forward, Turn Left/Right, etc.) into registers |
| **Activation** | Registers are executed in order; all players execute register 1, then register 2, etc. |
| **Conveyor Belts** | After each register, conveyor belts move any robot standing on them one space in the belt's direction |
| **Walls** | Block movement — a robot cannot move through a wall |
| **Checkpoints** | Numbered locations that must be reached in order (1 → 2 → 3 → ...) to win |
| **Winning** | The first player to reach all checkpoints in order wins the game |

### Key Model Classes

| Class | Role |
|-------|------|
| `Board` | Represents the game state: grid of spaces, list of players, current phase, current player |
| `Space` | A single tile on the board; holds a player reference, walls list, and field actions list |
| `Player` | A robot on the board; has a name, colour, position (space), heading, program registers, and hand cards |
| `Heading` | Enum: SOUTH, WEST, NORTH, EAST — with `next()` and `prev()` for turning |
| `Phase` | Enum representing the game phase: INITIALISATION, PROGRAMMING, ACTIVATION, PLAYER_INTERACTION |
| `Command` | Enum of command types: FORWARD, RIGHT, LEFT, FAST_FORWARD, etc. |
| `CommandCard` | A card holding a Command |
| `FieldAction` | Abstract base class for space actions (ConveyorBelt, Checkpoint) |

---

## Project Architecture Overview

The project follows the **MVC (Model-View-Controller)** pattern with an **Observer** pattern for UI updates:

```
┌─────────────────────────────────────────┐
│                  VIEW                    │
│  BoardView, SpaceView, PlayerView, ...  │
│  (observe model changes, render GUI)     │
└──────────────┬──────────────────────────┘
               │ observes (Subject/Observer)
┌──────────────▼──────────────────────────┐
│                 MODEL                    │
│  Board, Space, Player, Heading, ...     │
│  (game state, extends Subject)           │
└──────────────┬──────────────────────────┘
               │ manipulated by
┌──────────────▼──────────────────────────┐
│              CONTROLLER                  │
│  GameController, AppController,          │
│  BoardFactory, FieldAction subclasses    │
│  (game logic, board creation)            │
└─────────────────────────────────────────┘
```

- **Model** classes extend `Subject` and call `notifyChange()` whenever state changes.
- **View** classes implement `ViewObserver` and register with model objects via `attach()`.
- **Controller** classes modify the model; the observer mechanism automatically refreshes the views.

