package ai.Minimax.Experimental;

import ai.Minimax.MinimaxPlay;
import ai.Minimax.Node;
import ai.Minimax.Zobrist;
import game.Logic;
import game.Move;
import game.State;

import java.util.HashMap;

import static misc.Globals.RED;

public class FindWinnerStrategy {
    private static int team = RED;
    private static HashMap<Long, MinimaxPlay> transTable = new HashMap<>();
    private static int CURR_MAX_DEPTH;
    private static Node prevBestNode;

    private static void iterativeDeepeningMinimax(State state) {
        CURR_MAX_DEPTH = 0;
        prevBestNode = null;
        boolean done = false;
        MinimaxPlay bestPlay = null;
        long startTime = System.currentTimeMillis();
        while (!done) {
            Node simNode = new Node(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH+=1;
            bestPlay = minimax(simNode, CURR_MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH);
            System.out.println("CURRENT TABLE SIZE: " + transTable.size());

            if (Math.abs(bestPlay.score) >= 1000) done = true;
        }
        System.out.println("Score: " + bestPlay.score + ", Final Depth: " + CURR_MAX_DEPTH +
                ", Final table size: " + transTable.size() + ", Play:  oldRow: " + bestPlay.move.oldRow + ", oldCol: " +
                bestPlay.move.oldCol + ", newRow: " + bestPlay.move.newRow + ", newCol: " +
                bestPlay.move.newCol + ", team: " + bestPlay.move.team);
        System.out.println("TIME SPENT: " + (System.currentTimeMillis() - startTime));
    }

    public static MinimaxPlay minimax(Node node, int depth, int alpha, int beta) {
        Move bestMove = null;
        int score;
        int bestScore = (node.getState().getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        if (Logic.gameOver(node.getState()) || depth <= 0) {
            return new MinimaxPlay(bestMove, heuristic(node.getState()), depth);
        }
        MinimaxPlay transpoPlay;
        transpoPlay = transTable.get(node.getHashCode());
        if (transpoPlay != null && (depth <= transpoPlay.depth
                //|| Math.abs(transpoPlay.score) == 1000)) {
                && CURR_MAX_DEPTH-depth+transpoPlay.depth <= CURR_MAX_DEPTH ) ) {
            return transpoPlay;
        }


        if (depth == CURR_MAX_DEPTH && prevBestNode != null) {
            score = minimax(prevBestNode, depth - 1, alpha, beta).score;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = prevBestNode.getState().getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = prevBestNode.getState().getMove();
                }
                beta = Math.min(score, beta);
            }
        }

        for (Node child : node.getChildren()) {
            if (depth == CURR_MAX_DEPTH) if (child.equals(prevBestNode)) continue;
            score = minimax(child, depth - 1, alpha, beta).score;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
                alpha = Math.max(score, alpha);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
                beta = Math.min(score, beta);
            }
            if (beta <= alpha) break;
        }
        if (depth == CURR_MAX_DEPTH) {
            prevBestNode = node.getNextNode(bestMove);
        }
        if (transpoPlay == null || depth > transpoPlay.depth) {
        //if (transpoPlay == null && Math.abs(bestScore) >= 1000) {
            transTable.put(node.getHashCode(), new MinimaxPlay(bestMove, bestScore, depth));
        }

        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    private static int heuristic(State state) {
        if(Logic.gameOver(state)) {
            int winner = Logic.getWinner(state);

            if (winner == team) return 1000;
            else return -1000;
        }
        return 0;
    }

    public static void main(String[] args) {
        Zobrist.initialize();
        int pointsToWin = 2;
        System.out.println("Finding the optimal strategy when playing to " + pointsToWin + " points");
        State state = new State(pointsToWin);
        iterativeDeepeningMinimax(state);
    }
}
