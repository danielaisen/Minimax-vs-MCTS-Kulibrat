package game;

import misc.Globals;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static misc.Globals.BLACK;
import static misc.Globals.RED;

public class State {
    private int[][] board;
    private int turn;
    private int redScore;
    private int blackScore;
    private int scoreLimit;
    private int unplacedRed;
    private int unplacedBlack;
    private ArrayList<Move> legalMoves;
    private Move move;

    // Starting state
    public State(int scoreLimit) {
        int rows = Globals.bHeight;
        int columns = Globals.bWidth;
        board = new int[rows][columns];
        redScore = 0;
        blackScore = 0;
        if (Globals.losePieces) {
            unplacedRed = scoreLimit;
            unplacedBlack = scoreLimit;
        } else {
            unplacedRed = Globals.piece_amount;
            unplacedBlack = Globals.piece_amount;
        }
        turn = RED;
        this.scoreLimit = scoreLimit;
    }

    // Duplicate constructor
    public State(State state) {
        board = new int[state.board.length][];
        for (int i = 0; i < state.board.length; i++) {
            board[i] = Arrays.copyOf(state.board[i], state.board[i].length);
        }
        redScore = state.redScore;
        blackScore = state.blackScore;
        unplacedRed = state.unplacedRed;
        unplacedBlack = state.unplacedBlack;
        turn = state.turn;
        scoreLimit = state.scoreLimit;
        move = state.move;
    }

    public int[][] getBoard() {
        return board;
    }

    void setBoardEntry(int row, int col, int team) {
        board[row][col] = team;
    }

    public void addPoint(int team) {
        if (team == RED) {
            redScore++;
            if (!Globals.losePieces) unplacedRed++;
        } else {
            blackScore++;
            if (!Globals.losePieces) unplacedBlack++;
        }
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getScoreLimit() {
        return scoreLimit;
    }

    void addUnPlaced(int team) {
        if (team == RED) unplacedRed++;
        else unplacedBlack++;
    }

    void removeUnPlaced(int team) {
        if (team == RED) unplacedRed--;
        else unplacedBlack--;
    }

    public int getUnplaced(int team) {
        if (team == RED) return unplacedRed;
        else return unplacedBlack;
    }

    // Get a list of pieces/points from this state
    ArrayList<Point> getPieces(int team) {
        ArrayList<Point> entries = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == team) {
                    entries.add(new Point(j, i));
                }
            }
        }
        if (getUnplaced(team) > 0) {
            entries.add(new Point(-1, -1));
        }
        return entries;
    }

    public int getScore(int team) {
        return (team == RED) ? redScore : blackScore;
    }

    // Get the next state based on the input move
    State getNextState(Move m) {
        State state = new State(this);
        Logic.doTurn(m, state);
        state.move = m;
        return state;
    }

    // Creates and/or returns a list of new state objects which correspond to the children of the given state.
    public ArrayList<Move> getLegalMoves() {
        if (legalMoves != null) return legalMoves;
        legalMoves = Logic.legalMoves(turn, this);
        return legalMoves;
    }

    // Returns the material of a state, which is the value of the state based on various heuristics.
    // It checks the current turn, and outputs a positive number if it is good, or negative if bad
    public int getMaterial() {
        int score = 0;

        // Bonus for legal moves
        score += (getLegalMoves().size() * 2);

        // Bonus for being in front of opponent on your turn
        for (Point pR : getPieces(RED)) {
            for (Point pB : getPieces(BLACK)) {

                if (pR.x == pB.x && (pR.y - 1) == pB.y) {
                    score += 2;
                }
            }
        }
        // Win cycle bonus
        boolean bot = false;
        boolean mid = false;
        boolean top = false;
        int tempScore = 0;
        // RED
        for (Point p : getPieces(RED)) {
            if (p.x == 1) {
                if (p.y == 0) top = true;
                else if (p.y == 1) mid = true;
                else if (p.y == 2) bot = true;
            }
        }
        if (mid && (top || bot)) {
            tempScore += 20;
        }
        if (top && mid && bot) tempScore += 100;
        score += (turn == RED) ? tempScore : -tempScore;

        bot = false;
        mid = false;
        top = false;
        tempScore = 0;
        // BLACK
        for (Point p : getPieces(BLACK)) {
            if (p.x == 1) {
                if (p.y == 3) top = true;
                else if (p.y == 2) mid = true;
                else if (p.y == 1) bot = true;
            }
        }
        if (mid && (top || bot)) {
            tempScore += 20;
        }
        if (top && mid && bot) tempScore += 100;
        score += (turn == BLACK) ? tempScore : -tempScore;
        return score;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) return false;
        State state = (State) obj;
        return this == state || turn == state.getTurn() &&
                Arrays.deepEquals(board, state.board) &&
                redScore == state.getScore(RED) &&
                blackScore == state.getScore(BLACK);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(turn, redScore, blackScore);
        result = 31 * result + Arrays.deepHashCode(board);
        return result;

    }
}
