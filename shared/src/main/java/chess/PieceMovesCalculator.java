package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//Step In ChessMove if i say if if Queen return List of moves that it can go
//you start with myposition and move accordingly
//make it for each piece
//bishop : Move diagonally at any square unless end of the board or there is a piece there. if different team you can also take it.
// bishop
//while( not end of board )
// if (piece)
//  if (piece is mine) -> stop
//  else(if piece is other's) -> replace
// go diagonally once
//add to list
//


//public class PieceMovesCalculator {
//    public bishopMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece ){
//        int currentRow = myPosition.getRow();
//        int currentColumn = myPosition.getColumn();
//        int PossibleMoves[][] = {};
//        int PossibleMovesCheck[][] = {(currentRow+ 1,currentColumn + 1),(currentRow+ 1, currentColumn-1),(currentRow-1,currentColumn+1),(currentRow-1,currentColumn-1)};
//        for (int i : PossibleMovesCheck[][]){
//            while(i[0] < 8 && i[0] > 0 && i[0]> 0 && i[1] < 8){
//                if (board[currentRow, currentColumn] != null ){
//                    if (sameTeam){
//                        break;
//                    }
//                    else if (!sameTeam){
//                        PossibleMove.append(i);
//                    }
//                }
//            }
//        }
//
//
//
//
//    };
//
//}

public class PieceMovesCalculator {
    public static void exploreDirections(ChessBoard board, ChessPosition myPosition, ChessPiece piece, int drow, int dcol, boolean repeat, List<ChessMove> moves){

        int newRow = myPosition.getRow() + drow;
        int newCol = myPosition.getColumn() + dcol;
        while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8){
            ChessPosition newPosition = new ChessPosition (newRow, newCol);
            ChessPiece occupyingPiece = board.getPiece(newPosition);

            if(occupyingPiece == null){
                moves.add(new ChessMove(myPosition, newPosition, null));
            }else{
                if (piece.getTeamColor() != occupyingPiece.getTeamColor()){
                    moves.add(new ChessMove(myPosition, newPosition, null));

                }
                break;
            }


            if (!repeat) break;

            newRow = newRow + drow;
            newCol = newCol + dcol;
        }
    }
    public static Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] possibleDirection = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};


        for (int[] dir : possibleDirection) {
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);

        }
        return moves;


    }

    public static Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        int[][] direction = {{1,1}, {1,-1}, {-1,1}, {-1,-1}, {0,1}, {0,-1}, {1,0}, {-1,0}};
        List<ChessMove> moves = new ArrayList<> ();

        for (int[] dir : direction){
           exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }
    //return an array of moves
    // make a list of directions
    // make a list of moves that it could possibly take
    // for each direction you loop through
    // if adding that direction doesnt take you off the grid
    // if it is empty take that spot
    //if there's another piece take their spot
    //
    //return

    public static Collection<ChessMove> knightMove(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] possibleDirections = {{2, -1},{2, 1},{-2, 1},{-2, -1}, {-1, 2}, {-1, -2}, {1, -2}, {1, 2}};

        for (int[] dir : possibleDirections){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], false, moves);
        }
        return moves;
    }

    // if pieceColor is white go 1 in the x axis
    // if also check to see if there are any other team pieces on the corners if so add that to directions it could go
    // if its black go -1 in the x axis
    // same thing


    public static Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        List<ChessMove> moves = new ArrayList<>();
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int newRow = myPosition.getRow()+direction;
        int col = myPosition.getColumn();



        ChessPosition forward = new ChessPosition(newRow, col);
        if (board.getPiece(forward) == null){
            if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && newRow == 8) || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && newRow == 1)){
                for (ChessPiece.PieceType t : ChessPiece.PieceType.values()){
                    if (t != ChessPiece.PieceType.KING && t != ChessPiece.PieceType.PAWN){
                        moves.add(new ChessMove(myPosition, forward, t));
                    }
                }
            }
            else{
                moves.add(new ChessMove(myPosition,  forward, null));
            }
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2){
                int rowDouble = newRow + 1;
                ChessPosition twoForward = new ChessPosition(rowDouble, col);
                if (board.getPiece(twoForward) == null){
                    moves.add(new ChessMove(myPosition,twoForward, null));
                }
            } else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7){
                int rowDouble = newRow -1;
                ChessPosition twoForward = new ChessPosition(rowDouble, col);
                if (board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(myPosition, twoForward, null));
                }
            }



        }
        int[] diagonals = {col-1, col+1};

        for (int c : diagonals){
            if (c >=1 && c <=8 && newRow >= 1 && newRow <=8){
                ChessPosition diag = new ChessPosition(newRow, c);
                ChessPiece occupyingPiece = board.getPiece(diag);
                if (occupyingPiece != null && (occupyingPiece.getTeamColor() != piece.getTeamColor())){
                    if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && newRow == 8) || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && newRow == 1)){
                        for (ChessPiece.PieceType t : ChessPiece.PieceType.values()){
                            if (t != ChessPiece.PieceType.KING && t != ChessPiece.PieceType.PAWN){
                                moves.add(new ChessMove(myPosition, diag, t));
                            }
                        }
                    }
                    else{
                        moves.add(new ChessMove(myPosition,  diag, null));
                    }
                }
            }
        }
        return moves;
    }

    public static Collection<ChessMove> QueenMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece){
        List<ChessMove> moves = new ArrayList<>();
        int[][] direction = {{1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {-1,-1}, {1,-1}, {-1,1}};
        for(int[] dir : direction){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }

    public static Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] direction = {{1,0}, {-1,0}, {0,1},{0,-1}};
        for(int[] dir : direction){
            exploreDirections(board, myPosition, piece, dir[0], dir[1], true, moves);
        }
        return moves;
    }
}


