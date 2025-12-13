package ui;

import chess.*;

import java.util.Collection;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardDrawer {

    /**
     * Draws the initial board using the current game state.
     */
    public static void drawInitialBoard(boolean blackPerspective) {
        ChessGame game = new ChessGame(); // sets up all pieces
        drawBoard(game, blackPerspective);
    }

    /**
     * Draws the board for a game, either from white or black perspective.
     */
    public static void drawBoard(ChessGame game, boolean blackPerspective) {
        if (game == null) {
            System.out.println("No game to draw.");
            return;
        }

        System.out.print(EscapeSequences.ERASE_SCREEN);

        for (int row = blackPerspective ? 1 : 8;
             blackPerspective ? row <= 8 : row >= 1;
             row += blackPerspective ? 1 : -1) {

            StringBuilder line = new StringBuilder();
            line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);

            for (int col = blackPerspective ? 8 : 1;
                 blackPerspective ? col >= 1 : col <= 8;
                 col += blackPerspective ? -1 : 1) {

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = game.getBoard().getPiece(pos);

                String symbol;
                if (piece == null) {
                    symbol = EscapeSequences.EMPTY;
                } else {
                    symbol = switch (piece.getPieceType()) {
                        case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
                        case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
                        case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
                        case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
                        case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
                        case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
                    };
                }

                String bg = ((row + col) % 2 == 1) ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                line.append(bg).append(symbol).append(EscapeSequences.RESET_BG_COLOR);
            }

            System.out.println(line);
        }

        printColumnLetters(blackPerspective);
    }


    /**
     * Draws the board with highlighted squares.
     */
    public static void drawBoardWithHighlights(ChessGame game, boolean blackPerspective, Set<ChessPosition> highlights) {
        if (game == null) {
            System.out.println("No game to draw.");
            return;
        }

        System.out.print(ERASE_SCREEN);

        for (int row = blackPerspective ? 1 : 8; blackPerspective ? row <= 8 : row >= 1; row += blackPerspective ? 1 : -1) {
            StringBuilder line = new StringBuilder();
            line.append(SET_TEXT_COLOR_WHITE).append(row).append(" ").append(RESET_TEXT_COLOR);

            for (int col = blackPerspective ? 8 : 1; blackPerspective ? col >= 1 : col <= 8; col += blackPerspective ? -1 : 1) {
                ChessPosition pos = new ChessPosition(row, col);
                String piece = pieceSymbolAt(game, pos);
                String bg = squareBg(row, col);

                if (highlights.contains(pos)) {
                    bg = SET_BG_COLOR_YELLOW; // highlight in yellow
                }

                line.append(bg).append(piece).append(RESET_BG_COLOR);
            }
            System.out.println(line);
        }

        printColumnLetters(blackPerspective);
    }

    /**
     * Returns the Unicode symbol for a piece at a given position.
     */
    public static String pieceSymbolAt(ChessGame game, ChessPosition pos) {
        ChessPiece piece = game.getBoard().getPiece(pos);
        if (piece == null) return EscapeSequences.EMPTY;

        switch (piece.getPieceType()) {
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case KING:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            default:
                return EMPTY;
        }
    }

    /**
     * Returns the background color for a square.
     */
    private static String squareBg(int row, int col) {
        boolean light = ((row + col) % 2 == 1);
        return light ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY;
    }

    /**
     * Prints column letters at the bottom/top.
     */
    public static void printColumnLetters(boolean reversed) {
        if (!reversed) {
            System.out.println("   a  b  c  d  e  f  g  h");
        } else {
            System.out.println("   h  g  f  e  d  c  b  a");
        }
    }
}
