package ui;

import chess.*;

public class BoardDrawer {

    // Unicode starting rows for white and black
    private static final String[] WHITE_BACK_RANK = {
            EscapeSequences.WHITE_ROOK, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_QUEEN,
            EscapeSequences.WHITE_KING, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_ROOK
    };
    private static final String[] BLACK_BACK_RANK = {
            EscapeSequences.BLACK_ROOK, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_QUEEN,
            EscapeSequences.BLACK_KING, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_ROOK
    };

    public static void drawInitialBoard(boolean blackPerspective) {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (!blackPerspective) {
            drawWhitePerspective();
        }
        else {
            drawBlackPerspective();
        }
    }

    private static void drawWhitePerspective() {
        printColumnLetters(false);
        // row 8 down to 1
        for (int row = 8; row >= 1; row--) {
            StringBuilder line = new StringBuilder();
            line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
            for (int col = 1; col <= 8; col++) {
                int boardRow = row;
                int boardCol = col;
                String squareContent = pieceAt(boardRow, boardCol, false);
                String bg = squareBg(boardRow, boardCol);
                line.append(bg).append(squareContent).append(EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(line);
        }
        printColumnLetters(false);
    }

    private static void drawBlackPerspective() {
        printColumnLetters(true);
        // row 1 up to 8 (so a1 top-left)
        for (int row = 1; row <= 8; row++) {
            StringBuilder line = new StringBuilder();
            line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
            for (int col = 8; col >= 1; col--) {
                int boardRow = row;
                int boardCol = col;
                String squareContent = pieceAt(boardRow, boardCol, true);
                String bg = squareBg(boardRow, boardCol);
                line.append(bg).append(squareContent).append(EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(line);
        }
        printColumnLetters(true);
    }

    private static String pieceAt(int row, int col, boolean blackPerspective) {
        // White pawns
        if (row == 2) {
            return EscapeSequences.WHITE_PAWN;
        }
        // Black pawns
        if (row == 7) {
            return EscapeSequences.BLACK_PAWN;
        }
        // White back rank
        if (row == 1) {
            return WHITE_BACK_RANK[col - 1];
        }
        // Black back rank
        if (row == 8) {
            return BLACK_BACK_RANK[col - 1];
        }
        // Empty square
        return EscapeSequences.EMPTY;
    }


    private static String squareBg(int row, int col) {
        // light squares when (row + col) % 2 == 1
        boolean light = ((row + col) % 2 == 1);
        return light ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
    }

    public static void printColumnLetters(boolean reversed) {
        if (!reversed) {
            System.out.println("   a  b  c  d  e  f  g  h");
        } else {
            System.out.println("   h  g  f  e  d  c  b  a");
        }
    }

    public static void drawBoard(ChessGame game, boolean blackPerspective) {
        if (game == null) {
            System.out.println("No game to draw.");
            return;
        }

        System.out.print(EscapeSequences.ERASE_SCREEN);

        if (!blackPerspective) {
            printColumnLetters(false);
            for (int row = 8; row >= 1; row--) {
                StringBuilder line = new StringBuilder();
                line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
                for (int col = 1; col <= 8; col++) {
                    ChessPosition pos = new ChessPosition(row, col);
                    String piece = pieceSymbolAt(game, pos);
                    String bg = squareBg(row, col);
                    line.append(bg).append(piece).append(EscapeSequences.RESET_BG_COLOR);
                }
                System.out.println(line);
            }
            printColumnLetters(false);
        } else {
            printColumnLetters(true);
            for (int row = 1; row <= 8; row++) {
                StringBuilder line = new StringBuilder();
                line.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(row).append(" ").append(EscapeSequences.RESET_TEXT_COLOR);
                for (int col = 8; col >= 1; col--) {
                    ChessPosition pos = new ChessPosition(row, col);
                    String piece = pieceSymbolAt(game, pos);
                    String bg = squareBg(row, col);
                    line.append(bg).append(piece).append(EscapeSequences.RESET_BG_COLOR);
                }
                System.out.println(line);
            }
            printColumnLetters(true);
        }
    }

    /** Returns the Unicode symbol for a piece at a given position */
    private static String pieceSymbolAt(ChessGame game, ChessPosition pos) {
        ChessPiece piece = game.getBoard().getPiece(pos);
        if (piece == null) return EscapeSequences.EMPTY;

        switch (piece.getPieceType()) {
            case PAWN:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            case ROOK:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case KNIGHT: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case BISHOP: return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case QUEEN:  return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case KING:   return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            default:     return EscapeSequences.EMPTY;
        }
    }



}
