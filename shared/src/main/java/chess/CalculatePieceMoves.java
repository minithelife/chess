package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CalculatePieceMoves {

    public static void exploreDirections(
            ChessBoard board, ChessPosition myPosition,
            ChessPiece piece, int drow, int dcol,
            boolean repeat, List<ChessMove> moves) {

        int newRow = myPosition.getRow() + drow;
        int newCol = myPosition.getColumn() + dcol;

        while (newRow >= 1 && newCol >= 1 && newRow <= 8 && newCol <= 8) {
            ChessPosition newPos = new ChessPosition(newRow, newCol);
            ChessPiece occupyingPiece = board.getPiece(newPos);

            if (occupyingPiece == null) {
                moves.add(new ChessMove(myPosition, newPos, null));
            } else {
                if (piece.getTeamColor() != occupyingPiece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
                break;
            }

            if (!repeat) {
                break;
            }
            newRow += drow;
            newCol += dcol;
        }
    }

    public static Collection<ChessMove> bishopmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int[][] dirs = {{1, -1}, {-1, -1}, {1, 1}, {-1, 1}};
        for (int[] dir : dirs) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> rookmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> queenmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int[][] dirs = {
                {1, -1}, {-1, -1}, {1, 1}, {-1, 1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        for (int[] dir : dirs) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> kingmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int[][] dirs = {
                {1, -1}, {-1, -1}, {1, 1}, {-1, 1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        for (int[] dir : dirs) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> knightmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int[][] dirs = {
                {2, -1}, {2, 1}, {-2, 1}, {-2, -1},
                {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
        };
        for (int[] dir : dirs) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> pawnmoves(
            ChessBoard board, ChessPosition myPosition, ChessPiece piece) {

        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int dir = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
        int newRow = row + dir;

        addForwardMoves(board, myPosition, piece, moves, row, col, dir, newRow);
        addDiagonalMoves(board, myPosition, piece, moves, col, dir, newRow);

        return moves;
    }

    private static void addForwardMoves(
            ChessBoard board, ChessPosition pos, ChessPiece piece,
            List<ChessMove> moves, int row, int col, int dir, int newRow) {

        ChessPosition forward = new ChessPosition(newRow, col);
        ChessPiece frontPiece = board.getPiece(forward);
        if (frontPiece != null) {
            return;
        }

        if (isPromotion(piece, newRow)) {
            addPromotions(moves, pos, forward);
        } else {
            moves.add(new ChessMove(pos, forward, null));
        }

        boolean startRow =
                (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2)
                        || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);

        if (startRow) {
            ChessPosition doublePos = new ChessPosition(row + dir + dir, col);
            if (board.getPiece(doublePos) == null) {
                moves.add(new ChessMove(pos, doublePos, null));
            }
        }
    }

    private static void addDiagonalMoves(
            ChessBoard board, ChessPosition pos, ChessPiece piece,
            List<ChessMove> moves, int col, int dir, int newRow) {

        int[] diagonals = {1, -1};
        for (int d : diagonals) {
            int newCol = col + d;
            if (!inBounds(newRow, newCol)) {
                continue;
            }

            ChessPosition diagPos = new ChessPosition(newRow, newCol);
            ChessPiece diagPiece = board.getPiece(diagPos);
            if (diagPiece == null) {
                continue;
            }

            if (piece.getTeamColor() != diagPiece.getTeamColor()) {
                if (isPromotion(piece, newRow)) {
                    addPromotions(moves, pos, diagPos);
                } else {
                    moves.add(new ChessMove(pos, diagPos, null));
                }
            }
        }
    }

    private static void addPromotions(List<ChessMove> moves, ChessPosition from, ChessPosition to) {
        for (ChessPiece.PieceType t : ChessPiece.PieceType.values()) {
            if (t != ChessPiece.PieceType.KING && t != ChessPiece.PieceType.PAWN) {
                moves.add(new ChessMove(from, to, t));
            }
        }
    }

    private static boolean isPromotion(ChessPiece piece, int row) {
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 8)
                || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 1);
    }

    private static boolean inBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
