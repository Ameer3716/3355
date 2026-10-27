import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/** Dependency-free regression tests for Phase 2 money, movement and console flow. */
public class Phase2Tests {
    private static int passed;
    private static void check(String description, boolean condition) {
        if (!condition) { throw new AssertionError(description); }
        passed++;
        System.out.println("PASS: " + description);
    }
    private static Player cash(Player player, double amount) {
        player.addCash(amount - player.getCash());
        return player;
    }
    private static String capture(Runnable operation) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
            System.setOut(out);
            operation.run();
        } finally { System.setOut(original); }
        return bytes.toString("UTF-8");
    }
    private static String inputs(String seed) {
        StringBuilder text = new StringBuilder(seed).append('\n');
        for (int i = 1; i <= Game.NUM_PLAYERS; i++) {
            text.append("Player").append(i).append('\n').append(i % 2 == 0 ? "C\n" : "A\n");
        }
        return text.toString();
    }
    private static Game game() throws Exception { return new Game("board.txt", 42); }
    private static int occurrences(String text, String needle) {
        return (text.length() - text.replace(needle, "").length()) / needle.length();
    }
    public static void main(String[] args) throws Exception {
        Game g = game();
        Ownable brown = g.getBoard().getOwnable(1);
        Player cautious = cash(new CautiousBot("C"), 399);
        check("cautious refuses below $300 reserve", !cautious.wantsToBuy(brown));
        cautious.addCash(1);
        check("cautious accepts exactly $300 reserve", cautious.wantsToBuy(brown));
        Player aggressive = cash(new AggressiveBot("A"), 99);
        check("aggressive refuses unaffordable square", !aggressive.wantsToBuy(brown));
        aggressive.addCash(1);
        check("aggressive buys with exact funds", aggressive.wantsToBuy(brown));
        brownAsSquare(g, 1).land(aggressive, g);
        check("normal purchase updates cash, list, owner and net worth",
                aggressive.getCash() == 0 && brown.getOwner() == aggressive
                && aggressive.getOwnedSquares().size() == 1 && aggressive.getNetWorth() == 100);
        g.landOnOwnable(brown, aggressive);
        check("own-square landing changes nothing", aggressive.getCash() == 0
                && aggressive.getOwnedSquares().size() == 1);
        g.landOnOwnable(brown, cautious);
        check("single-property rent moves base rent between players",
                cautious.getCash() == 360 && aggressive.getCash() == 40);
        g.transfer(g.getBoard().getOwnable(2), null, aggressive, 0);
        g.transfer(g.getBoard().getOwnable(4), null, aggressive, 0);
        g.landOnOwnable(brown, cautious);
        check("full-property group doubles rent", cautious.getCash() == 280 && aggressive.getCash() == 120);
        ArrayList<Ownable> copy = aggressive.getOwnedSquares(); copy.clear();
        check("owned-square getter protects list", aggressive.getOwnedSquares().size() == 3);

        g = game(); cautious = cash(new CautiousBot("C"), 120);
        g.transfer(g.getBoard().getOwnable(1), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(2), null, cautious, 0);
        g.landOnOwnable(g.getBoard().getOwnable(4), cautious);
        check("cautious completes unowned group at normal price without reserve",
                cautious.getCash() == 0 && g.getBoard().countOwned(cautious, ColorGroup.BROWN) == 3);
        g = game(); cautious = cash(new CautiousBot("C"), 119);
        g.transfer(g.getBoard().getOwnable(1), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(2), null, cautious, 0);
        g.landOnOwnable(g.getBoard().getOwnable(4), cautious);
        check("completion cannot exceed cash", g.getBoard().getOwnable(4).getOwner() == null);

        g = game(); cautious = cash(new CautiousBot("C"), 180);
        aggressive = new AggressiveBot("A");
        g.transfer(g.getBoard().getOwnable(1), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(2), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(4), null, aggressive, 120);
        g.landOnOwnable(g.getBoard().getOwnable(4), cautious);
        check("cautious buy-out uses exact funds and no reserve",
                cautious.getCash() == 0 && aggressive.getCash() == 860
                && aggressive.getOwnedSquares().isEmpty() && cautious.getOwnedSquares().size() == 3);
        try {
            g.transfer(g.getBoard().getOwnable(4), aggressive, cautious, 0);
            throw new AssertionError("Stale seller accepted");
        } catch (IllegalArgumentException expected) {
            check("invalid transfer leaves ownership and money intact", cautious.getCash() == 0
                    && g.getBoard().getOwnable(4).getOwner() == cautious);
        }
        g = game(); cautious = cash(new CautiousBot("C"), 179);
        aggressive = new AggressiveBot("A");
        g.transfer(g.getBoard().getOwnable(1), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(2), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(4), null, aggressive, 120);
        g.landOnOwnable(g.getBoard().getOwnable(4), cautious);
        check("unaffordable buy-out falls back to base rent", cautious.getCash() == 123
                && aggressive.getCash() == 736 && g.getBoard().getOwnable(4).getOwner() == aggressive);

        g = game(); aggressive = new AggressiveBot("Station owner");
        cautious = new CautiousBot("Visitor");
        brownAsSquare(g, 7).land(aggressive, g);
        brownAsSquare(g, 7).land(cautious, g);
        check("station purchase and single-station $100 rent", aggressive.getCash() == 600
                && cautious.getCash() == 700);
        brownAsSquare(g, 17).land(aggressive, g);
        brownAsSquare(g, 7).land(cautious, g);
        check("both stations double rent to $200", aggressive.getCash() == 500 && cautious.getCash() == 500);
        g = game(); cautious = cash(new CautiousBot("C"), 450);
        aggressive = new AggressiveBot("A");
        g.transfer(g.getBoard().getOwnable(7), null, cautious, 0);
        g.transfer(g.getBoard().getOwnable(17), null, aggressive, 300);
        brownAsSquare(g, 17).land(cautious, g);
        check("station buy-out costs $450 and updates both lists", cautious.getCash() == 0
                && aggressive.getCash() == 950 && aggressive.getOwnedSquares().isEmpty());
        g = game(); cautious = cash(new CautiousBot("C"), 300);
        g.transfer(g.getBoard().getOwnable(7), null, cautious, 0);
        brownAsSquare(g, 17).land(cautious, g);
        check("last unowned station is normal $300 purchase", cautious.getCash() == 0
                && g.getBoard().countOwned(cautious, ColorGroup.RAILROAD) == 2);

        g = game(); aggressive = new AggressiveBot("Owner"); cautious = cash(new CautiousBot("Visitor"), 1);
        g.transfer(g.getBoard().getOwnable(1), null, aggressive, 100);
        g.landOnOwnable(g.getBoard().getOwnable(1), cautious);
        check("Phase 2 permits negative cash and does not release ownership", cautious.getCash() == -39
                && g.getBoard().getOwnable(1).getOwner() == aggressive);
        Game specials = game(); Player visitor = new AggressiveBot("Visitor");
        String messages = capture(() -> {
            for (int index : new int[] {0, 3, 5, 10, 13, 15}) {
                specials.getBoard().getSquare(index).land(visitor, specials);
            }
        });
        check("specials print messages without Phase 3 effects", visitor.getCash() == 800
                && visitor.getPosition() == 0 && visitor.getTurnsInJail() == 0
                && messages.contains("just visiting") && occurrences(messages, "Phase 3") == 3);

        Dice dice = new Dice(new Random(42)); Random expected = new Random(42);
        boolean diceMatch = true;
        for (int i = 0; i < 1000; i++) {
            int first = expected.nextInt(6) + 1, second = expected.nextInt(6) + 1;
            int sum = dice.roll();
            diceMatch &= sum == first + second && dice.getFirst() == first && dice.getSecond() == second;
        }
        check("1000 dice rolls match one seeded Random exactly", diceMatch);
        // Seed 42 first roll is 3+4; these destinations are special, avoiding purchases/rent.
        for (int[] test : new int[][] {{13, 0, 900}, {16, 3, 900}, {3, 10, 800}}) {
            Game movement = game(); Player mover = new AggressiveBot("Mover"); mover.setPosition(test[0]);
            movement.takeTurn(mover);
            check("movement and salary from " + test[0], mover.getPosition() == test[1] && mover.getCash() == test[2]);
        }
        Player byWorth = cash(new AggressiveBot("Z"), 1000);
        Player byCash = cash(new AggressiveBot("B"), 800);
        Player byName = cash(new AggressiveBot("A"), 800);
        Player invested = cash(new AggressiveBot("C"), 700);
        Game sorting = game(); sorting.transfer(sorting.getBoard().getOwnable(1), null, invested, 0);
        ArrayList<Player> ranks = new ArrayList<>(Arrays.asList(invested, byCash, byName, byWorth));
        Collections.sort(ranks);
        check("compareTo sorts worth descending, cash descending, then name",
                ranks.equals(Arrays.asList(byWorth, byName, byCash, invested)));

        Game full1 = game(), full2 = game();
        String firstRun = capture(() -> { checkStartup(full1, inputs("42")); full1.play(); });
        String secondRun = capture(() -> { checkStartup(full2, inputs("42")); full2.play(); });
        check("same seed and inputs produce byte-for-byte same 20-round output", firstRun.equals(secondRun));
        check("all 20 rounds, turns and leaderboards executed",
                occurrences(firstRun, "========== ROUND ") == Game.MAX_ROUNDS
                && occurrences(firstRun, " LEADERBOARD ----------") == Game.MAX_ROUNDS
                && occurrences(firstRun, "'s turn. Rolled") == Game.NUM_PLAYERS * Game.MAX_ROUNDS);
        boolean order = true;
        for (int i = 0; i < full1.getPlayers().size(); i++) {
            order &= full1.getPlayers().get(i).getName().equals("Player" + (i + 1));
        }
        check("leaderboard never reorders turn list", order);
        Game invalid = game();
        String invalidText = "letter\n999999999999999999999999\n42\n\nFirst\nX\na\n";
        for (int i = 2; i <= Game.NUM_PLAYERS; i++) { invalidText += "Player" + i + "\nc\n"; }
        final String supplied = invalidText;
        String recovery = capture(() -> checkStartup(invalid, supplied));
        check("invalid seed, overflow, blank name and bot type re-prompt safely",
                occurrences(recovery, "whole-number seed") == 2 && recovery.contains("non-empty name")
                && recovery.contains("Please enter C or A") && invalid.getPlayers().size() == Game.NUM_PLAYERS);
        Game random = game(); capture(() -> checkStartup(random, inputs("")));
        check("empty seed accepts random mode", random.getPlayers().size() == Game.NUM_PLAYERS);
        Game eof = game(); String eofOutput = capture(() -> {
            if (eof.startup(new Scanner("42\nName\n"))) { throw new AssertionError("EOF accepted"); }
        });
        check("EOF cancels without crashing or looping", eofOutput.contains("game cancelled"));
        testFractionalBuyouts();
        System.out.println("All " + passed + " Phase 2 checks passed.");
    }
    private static void testFractionalBuyouts() throws Exception {
        java.nio.file.Path boardFile = java.nio.file.Files.createTempFile("riyadhpoly-odd-", ".txt");
        try {
            String board = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("board.txt")),
                    java.nio.charset.StandardCharsets.UTF_8);
            board = board.replace("4,PROPERTY,Al-Oud,BROWN,120,56", "4,PROPERTY,Al-Oud,BROWN,121,56")
                    .replace("17,STATION,Grand Terminal,RAILROAD,300,100",
                            "17,STATION,Grand Terminal,RAILROAD,301,100");
            java.nio.file.Files.write(boardFile, board.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (boolean cautious : new boolean[] {false, true}) {
                for (double funds : new double[] {181, 181.5, 182}) {
                    Game game = new Game(boardFile.toString(), 42);
                    Player buyer = cash(cautious ? new CautiousBot("Buyer") : new AggressiveBot("Buyer"), funds);
                    Player seller = new AggressiveBot("Seller");
                    game.transfer(game.getBoard().getOwnable(1), null, buyer, 0);
                    game.transfer(game.getBoard().getOwnable(2), null, buyer, 0);
                    Ownable last = game.getBoard().getOwnable(4);
                    game.transfer(last, null, seller, 121);
                    String output = capture(() -> game.landOnOwnable(last, buyer));
                    if (funds < 181.5) {
                        check("odd-price buy-out rejects $181 for " + buyer.getClass().getSimpleName(),
                                last.getOwner() == seller && buyer.getCash() == 125 && seller.getCash() == 735
                                && !buyer.getOwnedSquares().contains(last) && seller.getOwnedSquares().contains(last));
                    } else {
                        check("odd-price buy-out pays exact $181.50 from $" + funds + " for " + buyer.getClass().getSimpleName(),
                                last.getOwner() == buyer && buyer.getCash() == funds - 181.5 && seller.getCash() == 860.5
                                && buyer.getOwnedSquares().contains(last) && seller.getOwnedSquares().isEmpty()
                                && buyer.getNetWorth() == funds - 181.5 + 321 && output.contains("$181.50"));
                    }
                }
            }
            Game game = new Game(boardFile.toString(), 42);
            Player buyer = cash(new CautiousBot("Buyer"), 451.5);
            Player seller = new AggressiveBot("Seller");
            game.transfer(game.getBoard().getOwnable(7), null, buyer, 0);
            Ownable terminal = game.getBoard().getOwnable(17);
            game.transfer(terminal, null, seller, 301);
            game.landOnOwnable(terminal, buyer);
            check("odd-price station buy-out pays exact $451.50", buyer.getCash() == 0
                    && seller.getCash() == 950.5 && terminal.getOwner() == buyer);
            Player laterBuyer = cash(new AggressiveBot("Later"), 452);
            // Transfer a half-dollar balance through another buy-out-sized payment.
            game.transfer(terminal, buyer, laterBuyer, 451.5);
            check("fractional cash survives later ownership transfers", laterBuyer.getCash() == 0.5
                    && buyer.getCash() == 451.5 && laterBuyer.getNetWorth() == 301.5);
            Player lower = cash(new AggressiveBot("A"), 300);
            Player higher = cash(new AggressiveBot("Z"), 300.5);
            check("leaderboard comparison preserves half-dollar differences", higher.compareTo(lower) < 0);
            String leaderboard = capture(() -> {
                if (!game.startup(new Scanner(inputs("42")))) { throw new AssertionError("startup"); }
                Player player = game.getPlayers().get(0);
                player.addCash(-499.5);
                game.printLeaderboard(1);
            });
            check("leaderboard displays fractional cash and net worth", leaderboard.contains("cash $300.50   net worth $300.50"));
            check("money formatting preserves whole-dollar samples and negative halves",
                    "800".equals(Money.format(800)) && "0.50".equals(Money.format(0.5))
                    && "-0.50".equals(Money.format(-0.5)));
            // Board accepts any nonnegative int rent; double it without int overflow.
            java.nio.file.Files.write(boardFile, board.replace("121,56", "121,1500000000")
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Game largeRent = new Game(boardFile.toString(), 42);
            Player owner = new AggressiveBot("Owner"), visitor = new AggressiveBot("Visitor");
            for (int index : new int[] {1, 2, 4}) {
                largeRent.transfer(largeRent.getBoard().getOwnable(index), null, owner, 0);
            }
            largeRent.landOnOwnable(largeRent.getBoard().getOwnable(4), visitor);
            check("doubled rent does not overflow a 32-bit integer", visitor.getCash() == -2999999200.0
                    && owner.getCash() == 3000000800.0);
            for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, -0.5}) {
                double before = visitor.getCash();
                try {
                    largeRent.transfer(largeRent.getBoard().getOwnable(4), owner, visitor, invalid);
                    throw new AssertionError("invalid payment accepted");
                } catch (IllegalArgumentException expected) {
                    check("invalid payment rejected without mutation: " + invalid,
                            visitor.getCash() == before && largeRent.getBoard().getOwnable(4).getOwner() == owner);
                }
            }
        } finally { java.nio.file.Files.deleteIfExists(boardFile); }
    }
    private static Square brownAsSquare(Game game, int index) { return game.getBoard().getSquare(index); }
    private static void checkStartup(Game game, String text) {
        if (!game.startup(new Scanner(text))) { throw new AssertionError("Startup cancelled"); }
    }
}
