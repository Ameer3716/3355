/** Required standalone check-in demonstration, with explicit checks (no -ea needed). */
public class RentTest {
    public static void main(String[] args) throws InvalidBoardException {
        Game game = new Game(args.length > 0 ? args[0] : "board.txt", 42);
        Player amal = new AggressiveBot("Amal");
        Player sara = new CautiousBot("Sara");
        Ownable shemaisy = game.getBoard().getOwnable(1);
        Ownable bathaa = game.getBoard().getOwnable(2);
        Ownable oud = game.getBoard().getOwnable(4);
        game.transfer(shemaisy, null, amal, 100);
        game.transfer(bathaa, null, amal, 100);
        game.transfer(oud, null, sara, 120);
        game.landOnOwnable(oud, amal);
        System.out.println("Owner of Al-Oud: " + oud.getOwner().getName());
        require(oud.getOwner() == amal && amal.getCash() == 420 && sara.getCash() == 860,
                "Buy-out must pay Sara $180 instead of rent.");
        require(amal.getOwnedSquares().contains(oud) && !sara.getOwnedSquares().contains(oud),
                "Both ownership lists must agree with the square.");
        game.landOnOwnable(bathaa, sara);
        System.out.println("Sara's cash: " + sara.getCash());
        require(sara.getCash() == 764 && amal.getCash() == 516, "Doubled rent must be $96.");
        System.out.println("RentTest passed: $180 buy-out, $96 doubled rent, Sara cash $764.");
    }
    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
