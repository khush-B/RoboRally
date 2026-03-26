package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.controller.ConveyorBelt;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * The visual representation of a single {@link Space} on the game board.
 * This view draws the background tile, any walls, field actions
 * (conveyor belts, checkpoints), and the player figure if present.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class SpaceView extends StackPane implements ViewObserver {

    /** The height of one space tile in pixels. */
    final public static int SPACE_HEIGHT = 40;
    /** The width of one space tile in pixels. */
    final public static int SPACE_WIDTH = 40;

    /** The underlying model space that this view represents. */
    public final Space space;

    /**
     * Creates a new SpaceView for the given {@link Space}. Sets up
     * the fixed size, background colour and registers itself as an
     * observer of the space so the view is updated automatically.
     *
     * @param space the model space to visualise
     */
    public SpaceView(@NotNull Space space) {
        this.space = space;

        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    /**
     * Draws the player figure (a coloured directional arrow) on the
     * space if a player occupies it.
     */
    private void updatePlayer() {
        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }
            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    /**
     * Draws all walls on this space. Each wall is rendered as a thick
     * red line along the corresponding edge of the tile.
     */
    private void drawWalls() {
        Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        for (Heading wall : space.getWalls()) {
            switch (wall) {
                case SOUTH:
                    gc.strokeLine(0, SPACE_HEIGHT - 1, SPACE_WIDTH, SPACE_HEIGHT - 1);
                    break;
                case NORTH:
                    gc.strokeLine(0, 1, SPACE_WIDTH, 1);
                    break;
                case WEST:
                    gc.strokeLine(1, 0, 1, SPACE_HEIGHT);
                    break;
                case EAST:
                    gc.strokeLine(SPACE_WIDTH - 1, 0, SPACE_WIDTH - 1, SPACE_HEIGHT);
                    break;
            }
        }
        this.getChildren().add(canvas);
    }

    /**
     * Draws all field actions (conveyor belts and checkpoints) that are
     * associated with this space.
     */
    private void drawFieldActions() {
        for (FieldAction action : space.getActions()) {
            if (action instanceof ConveyorBelt) {
                drawConveyorBelt((ConveyorBelt) action);
            } else if (action instanceof Checkpoint) {
                drawCheckpoint((Checkpoint) action);
            }
        }
    }

    /**
     * Draws a conveyor belt as a green directional arrow pointing in
     * the belt's heading direction.
     *
     * @param belt the conveyor belt to draw
     */
    private void drawConveyorBelt(ConveyorBelt belt) {
        Polygon arrow = new Polygon(
                0.0, 0.0,
                10.0, 16.0,
                20.0, 0.0);
        arrow.setFill(Color.LIGHTGREEN);
        arrow.setStroke(Color.GREEN);
        arrow.setStrokeWidth(1);
        arrow.setRotate((90 * belt.getHeading().ordinal()) % 360);
        this.getChildren().add(arrow);
    }

    /**
     * Draws a checkpoint as a yellow circle with the checkpoint number
     * displayed in the centre.
     *
     * @param checkpoint the checkpoint to draw
     */
    private void drawCheckpoint(Checkpoint checkpoint) {
        Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.YELLOW);
        double diameter = 24;
        double cx = (SPACE_WIDTH - diameter) / 2.0;
        double cy = (SPACE_HEIGHT - diameter) / 2.0;
        gc.fillOval(cx, cy, diameter, diameter);
        gc.setStroke(Color.DARKGOLDENROD);
        gc.setLineWidth(2);
        gc.strokeOval(cx, cy, diameter, diameter);
        this.getChildren().add(canvas);

        Text text = new Text(String.valueOf(checkpoint.getNumber()));
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        text.setFill(Color.BLACK);
        this.getChildren().add(text);
    }

    /**
     * Called when the observed {@link Space} changes. Clears all child
     * nodes and redraws walls, field actions, and the player figure.
     *
     * @param subject the subject that triggered the update
     */
    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            this.getChildren().clear();

            // Draw board features first, then the player on top
            drawFieldActions();
            drawWalls();
            updatePlayer();
        }
    }

}
