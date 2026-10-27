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
    private static Player cash(Player player, int amount) {
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
        System.out.println("All " + passed + " Phase 2 checks passed.");
    }
    private static Square brownAsSquare(Game game, int index) { return game.getBoard().getSquare(index); }
    private static void checkStartup(Game game, String text) {
        if (!game.startup(new Scanner(text))) { throw new AssertionError("Startup cancelled"); }
    }
}
