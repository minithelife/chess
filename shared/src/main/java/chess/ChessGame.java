package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Manages a chess game, making moves on a board.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard board;
    private ChessPosition enPassantTarget;
    private boolean hasMoved;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard(); // set up all pieces
        this.currentTurn = TeamColor.WHITE;
    }

    public enum TeamColor {
        WHITE, BLACK
    }

    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets all valid moves for a piece at a position.
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> moves = new HashSet<>(piece.pieceMoves(board, startPosition));

        // filter out moves that leave own king in check
        moves.removeIf(move -> {
            ChessBoard temp = board.copy();
            makeMoveOnBoard(temp, move);
            return isInCheck(piece.getTeamColor(), temp);
        });

        return moves;
    }

    /**
     * Makes a move in the actual game.
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position.");
        }
        if (piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Not your turn!");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move!");
        }



        // apply the move
        makeMoveOnBoard(board, move);

        // switch turn
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Checks if a team is in check.
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }

    /**
     * Checks if a team is in checkmate.
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;

        // if no legal moves exist, it's checkmate
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if a team is in stalemate.
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;

        // if no legal moves but not in check, stalemate
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ----------------- Helper Methods -----------------

    /**
     * Apply a move to a given board (does not check legality).
     */
    private void makeMoveOnBoard(ChessBoard b, ChessMove move) {
        ChessPiece movingPiece = b.getPiece(move.getStartPosition());

        // handle promotion
        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }

        // move the piece
        b.addPiece(move.getEndPosition(), movingPiece);
        b.addPiece(move.getStartPosition(), null); // clear old square
    }

    /**
     * Check if a team is in check on a given board state.
     */
    private boolean isInCheck(TeamColor teamColor, ChessBoard b) {
        ChessPosition kingPos = findKing(teamColor, b);
        if (kingPos == null) return false;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = b.getPiece(pos);
                if (piece == null || piece.getTeamColor() == teamColor) continue;

                Collection<ChessMove> moves = piece.pieceMoves(b, pos);
                for (ChessMove m : moves) {
                    if (m.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Find the king of a given team on a board.
     */
    private ChessPosition findKing(TeamColor teamColor, ChessBoard b) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = b.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    // ----------------- Object Overrides -----------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame)) return false;
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn &&
                Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }
}