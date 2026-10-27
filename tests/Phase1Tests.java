import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Standalone acceptance checks; no JUnit or other dependencies are needed. */
public class Phase1Tests {
    private static int passed = 0;

    private static void check(String description, boolean condition) {
        if (!condition) {
            throw new AssertionError(description);
        }
        passed++;
        System.out.println("PASS: " + description);
    }

    private static InvalidBoardException expectInvalid(String fileName, String... fragments)
            throws Exception {
        try {
            new Board(fileName);
        } catch (InvalidBoardException e) {
            for (String fragment : fragments) {
                if (!e.getMessage().contains(fragment)) {
                    throw new AssertionError("Missing error detail: " + fragment
                            + "; actual: " + e.getMessage());
                }
            }
            check("rejects " + fileName, true);
            return e;
        }
        throw new AssertionError("Expected InvalidBoardException for " + fileName);
    }

    private static void expectInvalidContent(List<String> lines, String... fragments)
            throws Exception {
        Path temporary = Files.createTempFile("riyadhpoly-invalid-", ".txt");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            expectInvalid(temporary.toString(), fragments);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static void main(String[] args) throws Exception {
        Board board = new Board("board.txt");
        check("loads exactly 20 squares", board.getSquares().size() == Board.BOARD_SIZE);
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            if (board.getSquare(i).getIndex() != i) {
                throw new AssertionError("Wrong board order at " + i);
            }
        }
        check("preserves indexes 0 through 19 in order", true);
        check("creates GoSquare", board.getSquare(0) instanceof GoSquare);
        check("creates ChanceSquare", board.getSquare(3) instanceof ChanceSquare);
        check("creates ParkSquare", board.getSquare(10) instanceof ParkSquare);
        check("creates JailSquare", board.getSquare(15) instanceof JailSquare);
        check("creates GoToJailSquare", board.getSquare(5) instanceof GoToJailSquare);
        check("creates PropertySquare implementing Ownable",
                board.getSquare(8) instanceof PropertySquare
                        && board.getSquare(8) instanceof Ownable);
        check("creates StationSquare implementing Ownable",
                board.getSquare(7) instanceof StationSquare
                        && board.getSquare(7) instanceof Ownable);

        Ownable olaya = (Ownable) board.getSquare(8);
        check("reads the supplied Olaya data", "Olaya".equals(board.getSquare(8).getName())
                && olaya.getPrice() == 220 && olaya.getBaseRent() == 112);
        check("builds all five group lists", board.getGroupSize(ColorGroup.BROWN) == 3
                && board.getGroupSize(ColorGroup.YELLOW) == 3
                && board.getGroupSize(ColorGroup.GREEN) == 3
                && board.getGroupSize(ColorGroup.BLUE) == 3
                && board.getGroupSize(ColorGroup.RAILROAD) == 2);
        Player first = new AggressiveBot("First");
        Player second = new CautiousBot("Second");
        for (String group : Arrays.asList(ColorGroup.BROWN, ColorGroup.YELLOW,
                ColorGroup.GREEN, ColorGroup.BLUE, ColorGroup.RAILROAD)) {
            check("countOwned is initially 0 for " + group, board.countOwned(first, group) == 0);
        }
        check("null is not counted as a player", board.countOwned(null, ColorGroup.BROWN) == 0);

        Ownable a = (Ownable) board.getSquare(1);
        Ownable b = (Ownable) board.getSquare(2);
        Game ownershipGame = new Game("board.txt", 42);
        ownershipGame.transfer(a, null, first, 0);
        ownershipGame.transfer(b, null, first, 0);
        check("countOwned uses the group map and current owners",
                board.countOwned(first, ColorGroup.BROWN) == 2);
        ownershipGame.transfer(a, first, second, 0);
        check("countOwned distinguishes two different players",
                board.countOwned(first, ColorGroup.BROWN) == 1
                        && board.countOwned(second, ColorGroup.BROWN) == 1);
        // Ownership tests are complete; reset the board for the remaining loader checks.
        board = new Board("board.txt");
        ArrayList<Square> copy = board.getSquares();
        copy.clear();
        check("getSquares cannot remove squares from the board",
                board.getSquares().size() == Board.BOARD_SIZE);

        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(printed)) {
            System.setOut(capture);
            board.printBoard();
        } finally {
            System.setOut(original);
        }
        String output = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        check("printBoard prints 20 lines including price and rent",
                output.trim().split("\\r?\\n").length == Board.BOARD_SIZE
                        && output.contains("8: Olaya (YELLOW) price $220, rent $112"));

        printed.reset();
        try (PrintStream capture = new PrintStream(printed)) {
            System.setOut(capture);
            Game landingGame = new Game("board.txt", 42);
            for (Square square : landingGame.getBoard().getSquares()) {
                square.land(first, landingGame);
            }
        } finally {
            System.setOut(original);
        }
        output = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        check("land dispatches through Square for every board square",
                output.contains("First landed on Olaya (YELLOW)")
                        && output.contains("Jail (just visiting)"));

        expectInvalid("tests/fixtures/invalid_missing_field.txt", "line 3", "expected 6 fields");
        expectInvalid("tests/fixtures/invalid_unknown_group.txt", "line 8", "unknown group", "WHITE");
        InvalidBoardException numeric = expectInvalid(
                "tests/fixtures/invalid_non_numeric_price.txt", "line 13", "price", "four hundred");
        check("wraps NumberFormatException as a checked exception",
                numeric.getCause() instanceof NumberFormatException);
        Path absentDirectory = Files.createTempDirectory("riyadhpoly-missing-");
        try {
            expectInvalid(absentDirectory.resolve("missing-board.txt").toString(), "cannot read");
        } finally {
            Files.deleteIfExists(absentDirectory);
        }

        List<String> supplied = Files.readAllLines(Paths.get("board.txt"), StandardCharsets.UTF_8);
        List<String> changed = new ArrayList<>(supplied);
        changed.set(9, "8,PROPERTY,Custom Landmark,YELLOW,221,113");
        Path custom = Files.createTempFile("riyadhpoly-custom-", ".txt");
        try {
            Files.write(custom, changed, StandardCharsets.UTF_8);
            Board customBoard = new Board(custom.toString());
            Ownable customSquare = (Ownable) customBoard.getSquare(8);
            check("uses changed file names, prices and rents without hard-coding",
                    customBoard.getSquare(8).getName().equals("Custom Landmark")
                            && customSquare.getPrice() == 221 && customSquare.getBaseRent() == 113);
        } finally {
            Files.deleteIfExists(custom);
        }
        changed = new ArrayList<>(supplied);
        changed.set(3, "2,UNKNOWN,Chance,,,");
        expectInvalidContent(changed, "line 4", "unknown type");
        changed = new ArrayList<>(supplied);
        changed.set(3, "1,PROPERTY,Duplicate,BROWN,100,40");
        expectInvalidContent(changed, "line 4", "expected square index 2");
        changed = new ArrayList<>(supplied);
        changed.remove(changed.size() - 1);
        expectInvalidContent(changed, "expected 20 squares but found 19");
        changed = new ArrayList<>(supplied);
        changed.add("20,PARK,Extra,,,");
        expectInvalidContent(changed, "too many squares");
        changed = new ArrayList<>(supplied);
        changed.add(1, "");
        changed.add(2, "   # another comment");
        changed.set(9, "6,PROPERTY,Al-Tahlia,WHITE,200,100");
        expectInvalidContent(changed, "line 10", "unknown group");

        System.out.println();
        System.out.println("All " + passed + " acceptance checks passed.");
    }
}
