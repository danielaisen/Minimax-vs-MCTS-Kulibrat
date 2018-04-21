package ai.Minimax;

import ai.AI;
import game.Logic;
import game.Move;
import game.State;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static misc.Globals.BLACK;
import static misc.Globals.RED;

public class LookupTableMinimax extends AI {
    private boolean useDB = true;
    private int CURR_MAX_DEPTH;
    private HashMap<Long, MinimaxPlay> transpoTable;
    private HashMap<Long, MinimaxPlay> lookupTable;
    private boolean done;

    private String JDBC_URL = "jdbc:derby:lookupDB;create=true";
    private Connection conn;

    public LookupTableMinimax(int team, State state, boolean overwriteDB) {
        super(team);
        transpoTable = new HashMap<>();
        lookupTable = new HashMap<>();

        if (useDB) {
            conn = getConnection(state.getPointsToWin());
            if (overwriteDB) {
                this.team = RED;
                System.out.println("Rebuilding lookup table. This will take some time.");
                buildLookupTable(state);
                try {
                    fillTable(state.getPointsToWin());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                this.team = team;
            }
        }

    }

    public Move makeMove(State state) {
        String teamstr = (team == BLACK) ? "BLACK" : "RED";
        System.out.println("Finding best play for " + teamstr);
        if (state.getLegalMoves().size() == 1) {
            return state.getLegalMoves().get(0);
        }
        // table lookup
        Node simNode = new Node(state);
        MinimaxPlay play;
        if (useDB) {
            play = queryData(simNode.getHashCode(), state.getPointsToWin());
        } else {
            play = iterativeDeepeningMinimax(state);
        }
        if (play == null) {
            System.err.println("DB Table is empty and needs to be rebuilt. Exiting");
            System.exit(0);
        }
        Move move = play.move;
        String winner = (play.score >= 1000) ? "RED" : "BLACK";
        System.out.println("BEST PLAY:  " + "oldRow: " + move.oldRow +
                ", oldCol: " + move.oldCol + ", newRow: " + move.newRow + ", newCol: " + move.newCol +
                ", WINNER IS: " + winner);
        return move;
    }

    private void buildLookupTable(State state) {
        long startTime = System.currentTimeMillis();
        iterativeDeepeningMinimax(state);
        System.out.println("Lookup table successfully built. Time spent: " + (System.currentTimeMillis() - startTime));
    }

    private MinimaxPlay iterativeDeepeningMinimax(State state) {
        CURR_MAX_DEPTH =0;
        done = false;
        int doneCounter = 0;
        MinimaxPlay play = null;
        while (!done) {
            Node simNode = new Node(state); // Start from fresh (Don't reuse previous game tree in new iterations)
            CURR_MAX_DEPTH += 1;
            int prevSize = lookupTable.size();

            play = minimax(simNode, CURR_MAX_DEPTH);
            System.out.println("CURRENT MAX DEPTH: " + CURR_MAX_DEPTH + ", LOOKUP TABLE SIZE: " + lookupTable.size() +
                    ", TRANSPO TABLE SIZE: " + transpoTable.size());
            if (lookupTable.size() == prevSize) {
                doneCounter++;
                if(doneCounter == 5) done = true;
                else doneCounter = 0;
                /*
                if(lookupTable.size() == transpoTable.size() && Math.abs(play.score) >= 1000) {
                    done = true;
                }
                */
            }



            if(Math.abs(play.score) >= 1000) {
                String player = (team == RED) ? "RED" : "BLACK";
                String opponent = (player.equals("RED")) ? "BLACK" : "RED";
                String winner = (play.score >= 1000) ? player : opponent;
                System.out.println("A SOLUTION HAS BEEN FOUND, WINNING STRAT GOES TO: " + winner);
            }
        }
        return play;
    }

    public MinimaxPlay minimax(Node node, int depth) {
        Move bestMove = null;
        int bestScore = (node.getState().getTurn() == team) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score;

        if (Logic.gameOver(node.getState()) || depth == 0) {
            return new MinimaxPlay(bestMove, heuristic(node.getState(), depth), depth);
        }
        MinimaxPlay lookupPlay = lookupTable.get(node.getHashCode());
        if(lookupPlay != null)
            return lookupPlay;
        MinimaxPlay transpoPlay = transpoTable.get(node.getHashCode());
        if (transpoPlay != null && depth <= transpoPlay.depth) {
            return transpoPlay;
        }
        boolean exploredChildren = true;
        for (Node child : node.getChildren()) {
            score = minimax(child, depth - 1).score;
            if(score == 0) exploredChildren = false;
            if (node.getState().getTurn() == team) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = child.getState().getMove();
                }
            }
            if(node.getHashCode() == 3160681311271971840L) {
                System.out.println("Child of first left:");
                System.out.println("Score: " + score);

            }
            if(node.getHashCode() == 486350317451566080L) {
                System.out.println("Child of second middle:");
                System.out.println("Score: " + score);
            }
            if(score == 0 && done) {
                System.out.println("SCORE: " + score);
                System.out.println("DEPTH: " + depth);
                System.out.println("CHILD IN LOOKUP: " + lookupTable.containsKey(child.getHashCode()));

            }
        }
        if(!exploredChildren && done) {
            System.out.println("BESTSCORE: " + bestScore);
        }
        if (transpoPlay == null || depth > transpoPlay.depth) {
            transpoTable.put(node.getHashCode(), new MinimaxPlay(bestMove, bestScore, depth));
        }
        MinimaxPlay play = lookupTable.get(node.getHashCode());
        if( (play == null && exploredChildren && Math.abs(bestScore) >= 1000)) {
            lookupTable.put(node.getHashCode(), new MinimaxPlay(bestMove, bestScore, depth));
        }
        return new MinimaxPlay(bestMove, bestScore, depth);
    }

    private int heuristic(State state, int depth) {
        int m = 2000;
        int n = CURR_MAX_DEPTH - depth;
        int opponent = (team == RED) ? BLACK : RED;
        int winner = Logic.getWinner(state);
        if (winner == team)
            //return 1000;
            return m-n;
        else if (winner == opponent)
            //return -1000;
            return -(m-n);
        return 0;
    }

    private Connection getConnection(int pointsToWin) {
        System.out.println("Connecting to database. This might take some time");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    JDBC_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Connection successful");

        // Creating the table, if it does not exist already
        String tableName = "plays_" + pointsToWin;
        try {
            conn.createStatement().execute("create table " + tableName +
                   "(id bigint primary key, oldRow smallint, oldCol smallint, newRow smallint, newCol smallint, team smallint, score smallint)");
        } catch (SQLException e) {
            System.out.println("Table '" + tableName + "' exists in the DB");
        }
        return conn;
    }

    private void fillTable(int pointsToWin) throws SQLException {
        System.out.println("Inserting data into table. This will take some time");
        String tableName = "plays_" + pointsToWin;
        long startTime = System.currentTimeMillis();
        conn.createStatement().execute("truncate table " + tableName);

        final int batchSize = 1000;
        int count = 0;
        PreparedStatement stmt = conn.prepareStatement("insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?)");
        for (Map.Entry<Long, MinimaxPlay> entry : lookupTable.entrySet()) {
            Long key = entry.getKey();
            MinimaxPlay value = entry.getValue();
            stmt.setLong(1, key);
            stmt.setInt(2, value.move.oldRow);
            stmt.setInt(3, value.move.oldCol);
            stmt.setInt(4, value.move.newRow);
            stmt.setInt(5, value.move.newCol);
            stmt.setInt(6, value.move.team);
            stmt.setInt(7, value.score);

            stmt.addBatch();
            if (++count % batchSize == 0) {
                stmt.executeBatch();
            }
        }
        stmt.executeBatch();
        stmt.close();
        System.out.println("Data inserted successfully. Time spent: " + (System.currentTimeMillis() - startTime));
    }

    private MinimaxPlay queryData(Long key, int pointsToWin) {
        MinimaxPlay play = null;
        String tableName = "plays_" + pointsToWin;
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select oldRow, oldCol, newRow, newCol, team, score from " + tableName + " where id=" + key);
            while(resultSet.next()) {
                Move move = new Move(resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5));
                int score = resultSet.getInt(6);
                play = new MinimaxPlay(move, score, 0);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Table '" + tableName + "' does not exist! Exiting.");
            System.exit(0);
        }
        return play;
    }
}

