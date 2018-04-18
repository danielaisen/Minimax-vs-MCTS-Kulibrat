package gui.info;

import game.State;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import misc.Globals;

import static misc.Globals.*;

public class InfoPane extends VBox {
    private ScoreBoard scoreBoard;
    private Circle turnCircle;
    private Label skippedTurn;
    private Label turnNumberLabel;
    private int prevTurn = 0;

    public InfoPane(int pointsToWin, int mode) {

        setPrefSize(Globals.WIDTH / 3, Globals.HEIGHT);
        setSpacing(40);
        setAlignment(Pos.CENTER);

        scoreBoard = new ScoreBoard();

        Label turnLabel = new Label("Turn: ");
        turnLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        turnCircle = new Circle(15);
        turnCircle.setFill(Color.RED);

        HBox turn = new HBox(turnLabel, turnCircle);
        turn.setAlignment(Pos.CENTER);

        turnNumberLabel = new Label("Turn number: 0");
        turnNumberLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        Label pointsToWinLabel = new Label("Points To Win: " + pointsToWin);
        pointsToWinLabel.setFont(Font.font("Verdana", 15));

        String modeText = (mode == HUMAN_VS_HUMAN) ? "Human vs. Human" :
                (mode == HUMAN_VS_AI) ? "Human vs. AI" : "AI vs. AI";
        Label modeLabel = new Label("Mode: " + modeText);
        modeLabel.setFont(Font.font("Verdana", 15));

        skippedTurn = new Label();
        skippedTurn.setFont(Font.font("Verdana", 15));


        VBox infoBox = new VBox(turn, turnNumberLabel, pointsToWinLabel, modeLabel, skippedTurn);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(15);


        getChildren().addAll(scoreBoard, infoBox);
    }

    public void update(State state, int turnNo) {
        scoreBoard.updateScore(state);
        updateTurn(state);
        turnNumberLabel.setText("Turn number: " + turnNo);

        if (prevTurn == state.getTurn()) {
            String team = (state.getTurn() == RED) ? "Black" : "Red";
            skippedTurn.setText("Team " + team + "'s turn has been skipped!");
        } else {
            skippedTurn.setText("");
        }
        prevTurn = state.getTurn();
    }

    private void updateTurn(State state) {
        if (state.getTurn() == RED) {
            turnCircle.setFill(Color.RED);

        } else {
            turnCircle.setFill(Color.BLACK);
        }
    }
}
