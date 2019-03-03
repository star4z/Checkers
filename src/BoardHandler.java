import java.util.Arrays;


/**
 * A backend for a game of checkers/draughts.
 */
public class BoardHandler {
    private int board[][];

    //TODO: force global jump
    //TODO: force jump with same piece on double jump

    //Color codes. Indicates the type of piece at a location
    final static int UNOCCUPIED = 0;
    final static int BLACK = 1;
    final static int WHITE = 2;
    final static int BLACK_KING = 3;
    final static int WHITE_KING = 4;

    //Status codes
    //Codes >= 0 are good
    final static int forceJump = -5;
    final static int insufficientPermissions = -4;
    final static int tooFar = -3;
    final static int usedSpace = -2;//
    final static int outOfBounds = -1;//one of the selected pieces is not within the bounds of the board
    final static int normal = 0;
    final static int goAgain = 1;

    private final static int[] badPos = new int[]{-1, -1};

    private boolean isBlackTurn = true;
    private int numWhites;
    private int numBlacks;

    private boolean isJumpForced;
    private int[] forcePos;

    void customGame() {
        board = new int[8][8];
        numWhites = 2;
        numBlacks = 1;

        board[5][4] = BLACK;

        board[6][1] = WHITE;

        board[6][3] = WHITE;
    }

    /**
     * This is called to create a new game.
     */
    public void newGame() {
        board = new int[8][8];
        numBlacks = 12;
        numWhites = 12;
        isJumpForced = false;

        /*
        Starting layout:
        [ ][B][ ][B][ ][B][ ][B]
        [B][ ][B][ ][B][ ][B][ ]
        [ ][B][ ][B][ ][B][ ][B]
        [ ][ ][ ][ ][ ][ ][ ][ ]
        [ ][ ][ ][ ][ ][ ][ ][ ]
        [W][ ][W][ ][W][ ][W][ ]
        [ ][W][ ][W][ ][W][ ][W]
        [W][ ][W][ ][W][ ][W][ ]

        'B' = BLACK
        'W' = WHITE
        ' ' = UNOCCUPIED

        NOTE: BLACK positions INCREASE while they go down the board;
        WHITE positions DECREASE while they go up the board.
         */
        board[0][1] = board[0][3] = board[0][5] = board[0][7] = BLACK;
        board[1][0] = board[1][2] = board[1][4] = board[1][6] = BLACK;
        board[2][1] = board[2][3] = board[2][5] = board[2][7] = BLACK;
        board[5][0] = board[5][2] = board[5][4] = board[5][6] = WHITE;
        board[6][1] = board[6][3] = board[6][5] = board[6][7] = WHITE;
        board[7][0] = board[7][2] = board[7][4] = board[7][6] = WHITE;
    }

    public UpdateStatus makeMove(int color, int startRow, int startCol, int endRow, int endCol) {
        int[] startPos = new int[]{startRow, startCol};
        int[] endPos = new int[]{endRow, endCol};
        return makeMove(color, startPos, endPos);
    }

    /**
     * Takes in the starting and ending coordinates for a turn and determines if they are valid.
     * Checking the validity of the move involves checking if the piece is the correct color.
     * If end position is not in the set of all single moves or the set of all single jumps,
     * move fails.
     *
     * @param color
     * @param startPos
     * @param endPos
     * @return
     */
    public UpdateStatus makeMove(int color, int[] startPos, int[] endPos) {
        String status = null;

        //
        if ((colorEquals(color, BLACK) && !isBlackTurn) || (colorEquals(color, WHITE) && isBlackTurn))
            return new UpdateStatus(insufficientPermissions, "It is not your turn.");

        if (!checkBounds(startPos[0]) || !checkBounds(startPos[1]))
            return new UpdateStatus(outOfBounds, "Pieces must be on the board to be used.");
        if (!checkBounds(endPos[0]) || !checkBounds(endPos[1]))
            return new UpdateStatus(outOfBounds, "Pieces may not be placed outside of the board.");

        if (!colorEquals(board[startPos[0]][startPos[1]], color)) {
            return new UpdateStatus(-3, "You do not have a piece there.");
        }

        if (!isEmpty(endPos))
            return new UpdateStatus(usedSpace, "That space is already occupied.");

        int[][] adjacents = getAvailableAdjacents(startPos[0], startPos[1]);
        int[][] singleJumps = getAvailableJumps(startPos[0], startPos[1]);
        switch (color) {
            case WHITE:
                if ((!Arrays.equals(badPos, singleJumps[0]) || !Arrays.equals(badPos, singleJumps[3])) &&
                        !Arrays.equals(endPos, singleJumps[0]) && !Arrays.equals(endPos, singleJumps[3]))
                    return new UpdateStatus(forceJump, "You must jump your opponent.");
                if (Arrays.equals(endPos, adjacents[0])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    status = "You moved to an adjacent tile.";

                } else if (Arrays.equals(endPos, adjacents[3])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    status = "You moved to an adjacent tile.";

                } else if (Arrays.equals(endPos, singleJumps[0])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    board[adjacents[0][0]][adjacents[0][1]] = UNOCCUPIED;
                    status = "You jumped an opponent.";
                    numBlacks--;
                    int[][] nextJumps = getAvailableJumps(singleJumps[0]);
                    if (endPos[0] == 0) {//handles reaching end of board
                        board[endPos[0]][endPos[1]] = WHITE_KING;
                        if (hasAvailableSpaces(nextJumps)) {
                            return new UpdateStatus(goAgain, status + " You can jump your opponent again.");
                        }
                    } else {
                        if (!Arrays.equals(nextJumps[0], badPos) || !Arrays.equals(nextJumps[3], badPos))
                            return new UpdateStatus(goAgain, "You can jump your opponent again.");
                    }
                } else if (Arrays.equals(endPos, singleJumps[3])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    board[adjacents[3][0]][adjacents[3][1]] = UNOCCUPIED;
                    status = "You jumped an opponent.";
                    numBlacks--;
                    int[][] nextJumps = getAvailableJumps(endPos);
                    if (endPos[0] == 0) {
                        board[endPos[0]][endPos[1]] = WHITE_KING;
                        if (hasAvailableSpaces(nextJumps))
                            return new UpdateStatus(goAgain, status + "You can jump your opponent again.");
                    } else {
                        if (!Arrays.equals(nextJumps[0], badPos) || !Arrays.equals(nextJumps[3], badPos))
                            return new UpdateStatus(goAgain, "You can jump your opponent again.");
                    }
                } else {
                    return new UpdateStatus(tooFar, "You are not allowed to move there.");
                }
                break;
            case BLACK:
                if ((!Arrays.equals(badPos, singleJumps[1]) || !Arrays.equals(badPos, singleJumps[2])) &&
                        !Arrays.equals(endPos, singleJumps[1]) && !Arrays.equals(endPos, singleJumps[2]))
                    return new UpdateStatus(forceJump, "You must jump your opponent.");
                if (Arrays.equals(endPos, adjacents[1])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    status = "You moved to an adjacent tile.";
                } else if (Arrays.equals(endPos, adjacents[2])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    status = "You moved to an adjacent tile.";
                } else if (Arrays.equals(endPos, singleJumps[1])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    board[adjacents[1][0]][adjacents[1][1]] = UNOCCUPIED;
                    status = "You jumped your opponent.";
                    numWhites--;
                    int[][] nextJumps = getAvailableJumps(singleJumps[1]);
                    if (endPos[0] == 7) {
                        board[endPos[0]][endPos[1]] = BLACK_KING;
                        if (hasAvailableSpaces(nextJumps))
                            return new UpdateStatus(goAgain, status + " You can jump your opponent again.");
                    } else {
                        if (!Arrays.equals(nextJumps[1], badPos) || !Arrays.equals(nextJumps[2], badPos))
                            return new UpdateStatus(goAgain, "You can jump your opponent again.");
                    }
                } else if (Arrays.equals(endPos, singleJumps[2])) {
                    board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                    board[startPos[0]][startPos[1]] = UNOCCUPIED;
                    board[adjacents[2][0]][adjacents[2][1]] = UNOCCUPIED;
                    status = "You jumped your opponent.";
                    numWhites--;
                    int[][] nextJumps = getAvailableJumps(endPos);
                    if (endPos[0] == 7) {
                        board[endPos[0]][endPos[1]] = BLACK_KING;
                        if (hasAvailableSpaces(nextJumps))
                            return new UpdateStatus(goAgain, status + " You can jump your opponent again.");
                    } else {
                        if (!Arrays.equals(nextJumps[1], badPos) || !Arrays.equals(nextJumps[2], badPos))
                            return new UpdateStatus(goAgain, status + " You can jump your opponent again.");
                    }
                } else {
                    return new UpdateStatus(tooFar, "You are not allowed to move there.");
                }
                break;
            case BLACK_KING:
            case WHITE_KING:
                boolean jumpsContainsEndPos = false;
                for (int[] pos : singleJumps) {
                    if (Arrays.equals(pos, endPos)) {
                        jumpsContainsEndPos = true;
                        break;
                    }
                }
                if (hasAvailableSpaces(singleJumps) && !jumpsContainsEndPos)
                    return new UpdateStatus(forceJump, "You must jump your opponent.");
                for (int i = 0; i < 4; i++) {
                    if (Arrays.equals(adjacents[i], endPos)) {
                        board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                        board[startPos[0]][startPos[1]] = UNOCCUPIED;
                        status = "You moved to an adjacent tile.";
                    }
                    if (Arrays.equals(singleJumps[i], endPos)) {
                        board[endPos[0]][endPos[1]] = board[startPos[0]][startPos[1]];
                        board[startPos[0]][startPos[1]] = UNOCCUPIED;
                        board[adjacents[i][0]][adjacents[i][1]] = UNOCCUPIED;
                        if (colorEquals(color, BLACK_KING))
                            numWhites--;
                        else if (colorEquals(color, WHITE_KING))
                            numBlacks--;
                        status = "You jumped your opponent.";
                        if (hasAvailableSpaces(getAvailableJumps(singleJumps[i])))
                            return new UpdateStatus(goAgain, status + "You can jump your opponent again.");
                    }
                }
                if (status == null) {
                    return new UpdateStatus(tooFar, "You are not allowed to move there.");
                }
                break;
            default:
                return new UpdateStatus(insufficientPermissions, "Invalid piece selected.");
        }

        if (color == BLACK && endPos[0] == 7)
            board[endPos[0]][endPos[1]] = BLACK_KING;
        if (color == WHITE && endPos[0] == 0)
            board[endPos[0]][endPos[1]] = WHITE_KING;
        this.isBlackTurn = !isBlackTurn;
        return new UpdateStatus(normal, status);//Proper exit
    }

    public UpdateStatus canMovePiece(int color, int[] pos) {
        if (isEmpty(pos))
            return new UpdateStatus(insufficientPermissions, "You do not have a piece there.");
        if (isBlackTurn != colorEquals(color, BLACK))
            return new UpdateStatus(insufficientPermissions, "You do not have a piece there.");

        if (isJumpForced) {
            //TODO
            if (!Arrays.equals(pos, forcePos))
                return new UpdateStatus(forceJump, "You have to move the piece you originally jumped with.");
        }
        return new UpdateStatus(normal, "Piece is okay to use.");
    }

    public int getPos(int[] pos) {
        if (!checkBounds(pos[0]) || !checkBounds(pos[1]))
            return -1;
        return board[pos[0]][pos[1]];
    }

    private boolean checkBounds(int pos) {
        return pos > -1 && pos < 8;
    }

    private boolean isEmpty(int[] pos){
        return checkBounds(pos[0]) && checkBounds(pos[1]) && board[pos[0]][pos[1]] == UNOCCUPIED;
    }

    private int[][] getAvailableJumps(int[] pos) {
        assert pos.length == 2;
        return getAvailableJumps(pos[0], pos[1]);
    }

    /**
     * Finds all jumps one jump away from the given space
     *
     * @param row current row of the piece
     * @param col current column of the piece
     * @return array of empty locations two spaces away from the piece diagonally, with an opposing piece between.
     */
    private int[][] getAvailableJumps(int row, int col) {
        int[][] solutions = new int[4][2];

        int[] adjacentSpaces = getAdjacents(row, col);
        for (int i = 0; i < 4; i++) {
            if (adjacentSpaces[i] != UNOCCUPIED && !colorEquals(adjacentSpaces[i], board[row][col])) {
                int[] jumpLoc = getAdjacentNo(i, getAdjacentNo(i, row, col));
                if (checkBounds(jumpLoc[0]) && checkBounds(jumpLoc[1]) && board[jumpLoc[0]][jumpLoc[1]] == UNOCCUPIED) {
                    solutions[i] = new int[]{jumpLoc[0], jumpLoc[1]};
                } else {
                    solutions[i] = badPos;
                }
            } else {
                solutions[i] = badPos;
            }
        }
        return solutions;
    }

    /**
     * Takes in an array of positions and determines if any are not equal to [-1,-1].
     *
     * @param posArray int[][], array of positions
     * @return true if there's any open spaces, otherwise false.
     */
    private boolean hasAvailableSpaces(int[][] posArray) {
        for (int[] nextPos : posArray) {
            if (!Arrays.equals(nextPos, badPos))
                return true;
        }
        return false;
    }

    private int[] getAdjacents(int[] loc) {
        assert loc.length == 2;
        return getAdjacents(loc[0], loc[1]);
    }

    /**
     * Returns values of adjacent items
     * Returns -1 if the value is out of bounds
     * Returns in order
     *
     * @param row of item to check
     * @param col of item to check
     * @return values of adjecent items
     */
    private int[] getAdjacents(int row, int col) {
        int[] adjacents = new int[4];
        if (checkBounds(col - 1)) {
            if (checkBounds(row - 1))
                adjacents[0] = board[row - 1][col - 1];
            else
                adjacents[0] = -1;
            if (checkBounds(row + 1))
                adjacents[1] = board[row + 1][col - 1];
            else adjacents[1] = -1;
        } else {
            adjacents[0] = -1;
            adjacents[1] = -1;
        }
        if (checkBounds(col + 1)) {
            if (checkBounds(row + 1))
                adjacents[2] = board[row + 1][col + 1];
            else
                adjacents[2] = -1;
            if (checkBounds(row - 1))
                adjacents[3] = board[row - 1][col + 1];
            else adjacents[3] = -1;
        } else {
            adjacents[2] = -1;
            adjacents[3] = -1;
        }
        return adjacents;
    }

    private int[] getAdjacentNo(int pos, int[] loc) {
        assert loc.length == 2;
        return getAdjacentNo(pos, loc[0], loc[1]);
    }

    private int[] getAdjacentNo(int pos, int row, int col) {
        switch (pos) {
            case 0:
                return new int[]{row - 1, col - 1};
            case 1:
                return new int[]{row + 1, col - 1};
            case 2:
                return new int[]{row + 1, col + 1};
            case 3:
                return new int[]{row - 1, col + 1};
            default:
                return new int[]{-1, -1};
        }
    }

    private int[][] getAvailableAdjacents(int[] pos) {
        assert pos.length == 2;
        return getAvailableAdjacents(pos[0], pos[1]);
    }

    private int[][] getAvailableAdjacents(int row, int col) {
        int[][] adjacents = new int[4][2];
        if (checkBounds(col - 1)) {
            if (checkBounds(row - 1))
                adjacents[0] = new int[]{row - 1, col - 1};
            else
                adjacents[0] = badPos;
            if (checkBounds(row + 1))
                adjacents[1] = new int[]{row + 1, col - 1};
            else
                adjacents[1] = badPos;
        } else {
            adjacents[0] = badPos;
            adjacents[1] = badPos;
        }
        if (checkBounds(col + 1)) {
            if (checkBounds(row + 1))
                adjacents[2] = new int[]{row + 1, col + 1};
            else
                adjacents[2] = badPos;
            if (checkBounds(row - 1))
                adjacents[3] = new int[]{row - 1, col + 1};
            else adjacents[3] = badPos;
        } else {
            adjacents[2] = badPos;
            adjacents[3] = badPos;
        }
        return adjacents;
    }

    private boolean colorEquals(int color1, int color2) {
        return (color1 % 2 == color2 % 2 && color1 != UNOCCUPIED && color2 != UNOCCUPIED);
//        return ((color1 == BLACK || color1 == BLACK_KING) && (color2 == BLACK || color2 == BLACK_KING)) ||
//                ((color1 == WHITE || color1 == WHITE_KING) && (color2 == WHITE || color2 == WHITE_KING));
    }

    public boolean isBlackTurn() {
        return isBlackTurn;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getNumBlacks() {
        return numBlacks;
    }

    public int getNumWhites() {
        return numWhites;
    }

    public class UpdateStatus {
        String message;
        int opCode;

        UpdateStatus(int opCode, String string) {
            this.opCode = opCode;
            this.message = string;
        }

        @Override
        public String toString() {
            return "Status(" + opCode + "): " + message;
        }
    }

}
