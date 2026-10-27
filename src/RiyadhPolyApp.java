import java.util.Scanner;

public class RiyadhPolyApp {
    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "board.txt";
        try (Scanner input = new Scanner(System.in)) {
            Game game = new Game(fileName);
            if (game.startup(input)) { game.play(); }
        } catch (InvalidBoardException e) {
            System.err.println("Error loading " + fileName + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
