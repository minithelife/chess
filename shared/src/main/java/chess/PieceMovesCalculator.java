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
}

