package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CalculatePieceMoves {
    public static void exploreDirections(ChessBoard board, ChessPosition myPosition, ChessPiece piece, int drow, int dcol, boolean repeat, List<ChessMove> moves){
        int newRow = myPosition.getRow() + drow;
        int newCol = myPosition.getColumn() + dcol;
        while(newRow >= 1 && newCol >= 1 && newRow<=8 && newCol <= 8){
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece occupyingPiece = board.getPiece(newPosition);
            if(occupyingPiece == null){
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            else{
                if(piece.getTeamColor() != occupyingPiece.getTeamColor()){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                if(piece.getTeamColor() == occupyingPiece.getTeamColor()){
                    break;
                }
            }
            if(!repeat) break;
            newRow = newRow + drow;
            newCol = newCol + dcol;
        }
    }
    public static Collection<ChessMove> BishopMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1,-1}, {-1,-1}, {1,1}, {-1,1}};
        for(int[] dir : directions){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }
    public static Collection<ChessMove> KingMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1,-1}, {-1,-1}, {1,1}, {-1,1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        for(int[] dir : directions){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }
    public static Collection<ChessMove> KnightMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{2,-1}, {2,1}, {-2,1}, {-2,-1}, {1,2}, {-1,2}, {1,-2}, {-1,-2}};
        for(int[] dir : directions){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }
    public static Collection<ChessMove> PawnMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int direction = (ChessGame.TeamColor.WHITE == piece.getTeamColor()) ? 1 : -1;
        int newRow = row + direction;

        ChessPosition forward =  new ChessPosition(newRow, col);
        ChessPiece forwardPiece = board.getPiece(forward);
        if(forwardPiece == null){
            if((piece.getTeamColor().equals(ChessGame.TeamColor.WHITE) && newRow==8) || (piece.getTeamColor().equals(ChessGame.TeamColor.BLACK) && newRow==1)){
                for(ChessPiece.PieceType t: ChessPiece.PieceType.values()){
                    if(t != ChessPiece.PieceType.KING &&  t != ChessPiece.PieceType.PAWN){
                        moves.add(new ChessMove(myPosition, forward, t));
                    }
                }
            }
            else{
                moves.add(new ChessMove(myPosition, forward, null));
            }
            if((piece.getTeamColor().equals(ChessGame.TeamColor.WHITE) && row==2) || (piece.getTeamColor().equals(ChessGame.TeamColor.BLACK) && row==7)){
                ChessPosition doubleRow = new ChessPosition(row + direction + direction, col);
                ChessPiece doublePiece = board.getPiece(doubleRow);
                if(doublePiece == null){
                    moves.add(new ChessMove(myPosition, doubleRow, null));
                }
            }
        }
        //if diagonal. if not null and if 8 then promote. else break;
        int[] diagonal = {1, -1};
        for (int diag : diagonal){
            int diagCol = col + diag;
            ChessPosition diagonalPosition = new ChessPosition(newRow, diagCol);
            if(diagCol <= 8 && diagCol >= 1 && newRow <= 8 && newRow >= 1){
                ChessPiece diagonalPiece = board.getPiece(diagonalPosition);
                if(diagonalPiece != null && piece.getTeamColor() != diagonalPiece.getTeamColor()){
                    if((piece.getTeamColor().equals(ChessGame.TeamColor.WHITE) && newRow==8) || (piece.getTeamColor().equals(ChessGame.TeamColor.BLACK) && newRow==1)) {
                        for(ChessPiece.PieceType t: ChessPiece.PieceType.values()){
                            if(t != ChessPiece.PieceType.KING &&  t != ChessPiece.PieceType.PAWN){
                                moves.add(new ChessMove(myPosition, diagonalPosition, t));
                            }
                        }
                    }
                    else{
                        moves.add(new ChessMove(myPosition, diagonalPosition,  null));
                    }
                }
            }
        }
        return moves;
        //if forward is null. if 8 then promote. else. normal. if at 2. double
    }
    public static Collection<ChessMove> QueenMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {{1,-1}, {-1,-1}, {1,1}, {-1,1}, {1,0}, {-1,0}, {0,1}, {0,-1}};
        for(int[] dir : directions){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }
    public static Collection<ChessMove> RookMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = { {1,0}, {-1,0}, {0,1}, {0,-1}};
        for(int[] dir : directions){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }
}
