package ai.Minimax;

import java.util.Random;

import static misc.Globals.RED;

public class Zobrist {
    public static long[][][] board;
    public static long[] turn;
    public static long[] redPoints;
    public static long[] blackPoints;

    public static void initialize() {
        long range = Long.MAX_VALUE;
        Random r = new Random();
        r.setSeed(0);
        // board keys
        int rows = 4;
        int cols = 3;
        int unique_pieces = 3;
        board = new long[rows][cols][unique_pieces];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i][j].length; k++) {
                    long rLong = (long) (r.nextDouble() * range);
                    board[i][j][k] = rLong;
                }
            }
        }
        // turn keys
        turn = new long[3];
        for (int i = 0; i < turn.length; i++) {
            turn[i] = (long) (r.nextDouble() * range);
        }
        // points keys
        redPoints = new long[11];
        blackPoints = new long[11];
        for (int i = 0; i < 10; i++) {
            redPoints[i] = (long) (r.nextDouble() * range);
            blackPoints[i] = (long) (r.nextDouble() * range);
        }
    }

    public static long[] points(int team) {
        return (team == RED) ? redPoints : blackPoints;
    }
}
