package chess;

import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
         return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch(type){
            case BISHOP:
                return CalculatePieceMoves.BishopMoves(board, myPosition, this);
            case KING:
                return CalculatePieceMoves.KingMoves(board, myPosition, this);
            case KNIGHT:
                return CalculatePieceMoves.KnightMoves(board, myPosition, this);
            case PAWN:
                return CalculatePieceMoves.PawnMoves(board, myPosition, this);
            case QUEEN:
                return CalculatePieceMoves.QueenMoves(board, myPosition, this);
            case ROOK:
                return CalculatePieceMoves.RookMoves(board, myPosition, this);
            default:
                return List.of();
        }
    }

    @Override
    public int hashCode() {
        int result = 31 * ( pieceColor!= null ?  pieceColor.hashCode() : 0 );
        result = 31 * result + (type != null ? type.hashCode() : 0 );
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        ChessPiece  other = (ChessPiece) obj;
        return this.pieceColor == other.pieceColor &&  this.type == other.type;
    }
}
