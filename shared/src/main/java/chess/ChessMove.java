package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {

        return promotionPiece;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", startPosition, endPosition, promotionPiece);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // same object in memory
        if (!(obj instanceof ChessMove)) return false; // not even the same type
        ChessMove other = (ChessMove) obj; // cast to ChessMove

        // Compare start and end positions and promotion piece
        boolean positionsEqual = startPosition.equals(other.startPosition)
                && endPosition.equals(other.endPosition);

        boolean promotionEqual = (promotionPiece == null && other.promotionPiece == null)
                || (promotionPiece != null && promotionPiece == other.promotionPiece);

        return positionsEqual && promotionEqual;
    }
//    if my answer equals mys object return true
//    if my object is not an instance of chessmove return false
//    cast (chessmove) obj to a chessmove
//        the boolean returns if the positions are equal and the promotions are equal



    @Override
    public int hashCode() {
        int result = startPosition.hashCode(); // start position contributes to hash
        result = 31 * result + endPosition.hashCode(); // end position
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0); // promotion piece
        return result;
    }
//    make a new hascode called result with the hashcode of the startpostion.
//    then you multiply it by 31 and add the end position,
//    then multiply 31 to that and add the promotionpiece hashcode if not null, if is null add 0.
}
