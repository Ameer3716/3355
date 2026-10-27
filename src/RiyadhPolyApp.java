
public class RiyadhPolyApp {
    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "board.txt";
        try {
            Board board = new Board(fileName);
            System.out.println("RiyadhPoly - Phase 1: Design and Board");
            System.out.println("Board loaded: " + board.getSquares().size() + " squares.");
            board.printBoard();
        } catch (InvalidBoardException e) {
            System.err.println("Error loading " + fileName + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
