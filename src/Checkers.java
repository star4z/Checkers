import java.util.Scanner;

public class Checkers {
    public static void main(String[] args) {
        BoardHandler boardHandler = new BoardHandler();
        boardHandler.newGame();
//        boardHandler.customGame();
        Scanner kb = new Scanner(System.in);

        System.out.println("You have begun a new game of Checkers.");
        System.out.println("Solid pieces are black. All rows and columns are numbers 0 - 7.");
        boolean finished = false;

        boolean isBlackTurn = boardHandler.isBlackTurn();
        while (!finished) {
            if (isBlackTurn) {
                System.out.println("It's Black's turn.");
            } else {
                System.out.println("It's White's turn.");
            }
            printBoard(boardHandler);

            try {
                int[] startPos = new int[2];
                int[] endPos = new int[2];
                System.out.println("Which piece would you like to move? (row col)");
                String n = kb.nextLine();
                if (!getPos(n, startPos)){
                    throw new NumberFormatException();
                }
                System.out.println("Where would you like to move the piece at [" + startPos[0] + ", " + startPos[1] + "] to?");
                n = kb.nextLine();
                if (!getPos(n, endPos)){
                    throw new NumberFormatException();
                }

                //Make move and receive status from boardHandler
                BoardHandler.UpdateStatus status = boardHandler.makeMove(boardHandler.getPos(startPos), startPos, endPos);
                isBlackTurn = boardHandler.isBlackTurn();
                if (status.opCode == BoardHandler.normal) {
                    System.out.println(status.message);
                } else {
                    System.out.println(status.message);
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println("Input was not in form \"row col\".");
                System.out.println("Press [Enter] to continue.");
                kb.nextLine();
            }

            //Check to see if anyone has won
            if (boardHandler.getNumWhites() == 0) {
                System.out.println("The game is finished. Black wins!");
                printBoard(boardHandler);
                finished = true;
            }
            if (boardHandler.getNumBlacks() == 0) {
                System.out.println("The game is finished. White wins!");
                printBoard(boardHandler);
                finished = true;
            }
        }
    }

    private static void printBoard(BoardHandler h) {
        int[][] board = h.getBoard();
        System.out.println("\t 0\t 1\t 2\t 3\t 4\t 5\t 6\t 7");
        for (int i = 0; i < board.length; i++) {
            System.out.print("   " + i);
            for (int j : board[i]) {
                String c;
                switch (j) {
                    case BoardHandler.BLACK:
                        c = "⬤";
                        break;
                    case BoardHandler.WHITE:
                        c = "○";
                        break;
                    case BoardHandler.BLACK_KING:
                        c = "◉";
                        break;
                    case BoardHandler.WHITE_KING:
                        c = "◎";
                        break;
                    default:
                        c = " ";
                }
                System.out.print("[" + c + "]\t");
            }
            System.out.println();
        }
    }

    private static boolean getPos(String input, int[] a){
        String [] splitString = input.split(" ");
        if (splitString.length < 2)
            return false;
        else {
            a[0] = Integer.parseInt(splitString[0]);
            a[1] = Integer.parseInt(splitString[1]);
            return true;
        }
    }
}
