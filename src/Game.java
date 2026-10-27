import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Game {
    public static final int NUM_PLAYERS = 4;
    public static final int SALARY = 100;
    public static final int MAX_ROUNDS = 20;
    public static final double BUYOUT_FACTOR = 1.5;
    public static final int FULL_GROUP_RENT_FACTOR = 2;

    private final Board board;
    private final ArrayList<Player> players = new ArrayList<>();
    private Dice dice;

    public Game(String boardFile) throws InvalidBoardException {
        board = new Board(boardFile);
    }
    public Game(String boardFile, long seed) throws InvalidBoardException {
        this(boardFile);
        dice = new Dice(new Random(seed));
    }
    public Board getBoard() { return board; }
    public ArrayList<Player> getPlayers() { return new ArrayList<>(players); }

    /** Returns false on EOF, so redirected or incomplete input never loops forever. */
    public boolean startup(Scanner input) {
        System.out.println("**************************************************");
        System.out.println("**             Welcome to RiyadhPoly             **");
        System.out.println("**************************************************");
        System.out.println("Board loaded: " + board.getSquares().size() + " squares.");
        while (true) {
            String seed = readLine(input, "Seed (press Enter for random): ");
            if (seed == null) { return false; }
            try {
                dice = new Dice(seed.isEmpty() ? new Random() : new Random(Long.parseLong(seed)));
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole-number seed or press Enter.");
            }
        }
        players.clear();
        for (int i = 1; i <= NUM_PLAYERS; i++) {
            String name;
            do {
                name = readLine(input, "Player " + i + " name: ");
                if (name == null) { return false; }
                if (name.isEmpty()) { System.out.println("Please enter a non-empty name."); }
            } while (name.isEmpty());
            while (true) {
                String type = readLine(input, "Player " + i + " type (C = cautious, A = aggressive): ");
                if (type == null) { return false; }
                if ("C".equalsIgnoreCase(type)) { players.add(new CautiousBot(name)); break; }
                if ("A".equalsIgnoreCase(type)) { players.add(new AggressiveBot(name)); break; }
                System.out.println("Please enter C or A.");
            }
        }
        System.out.print("Playing order: ");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) { System.out.print(" -> "); }
            System.out.print(players.get(i).getName());
        }
        System.out.println();
        return true;
    }

    private String readLine(Scanner input, String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {
            System.out.println("\nInput ended; game cancelled.");
            return null;
        }
        return input.nextLine().trim();
    }

    public void takeTurn(Player player) {
        if (dice == null) { throw new IllegalStateException("Set up the dice before playing."); }
        int steps = dice.roll();
        int oldPosition = player.getPosition();
        int newPosition = (oldPosition + steps) % Board.BOARD_SIZE;
        System.out.println(player.getName() + "'s turn. Rolled " + dice.getFirst()
                + " + " + dice.getSecond() + " = " + steps + ". Moving to square "
                + newPosition + ".");
        player.setPosition(newPosition);
        // Two six-sided dice cannot traverse the entire 20-square board in one roll.
        if (newPosition < oldPosition) {
            player.addCash(SALARY);
            System.out.println(player.getName() + " passed GO and collected $" + SALARY + ".");
        }
        board.getSquare(newPosition).land(player, this);
    }

    public void play() {
        if (players.isEmpty()) { throw new IllegalStateException("Set up players before playing."); }
        for (int round = 1; round <= MAX_ROUNDS; round++) {
            System.out.println("\n========== ROUND " + round + " ==========");
            for (Player player : players) { takeTurn(player); }
            printLeaderboard(round);
        }
    }

    public void printLeaderboard(int round) {
        // Sort a copy: leaderboard order must never change the playing order.
        ArrayList<Player> standings = new ArrayList<>(players);
        Collections.sort(standings);
        System.out.println("---------- ROUND " + round + " LEADERBOARD ----------");
        for (int i = 0; i < standings.size(); i++) {
            Player player = standings.get(i);
            System.out.println((i + 1) + ". " + player.getName() + "    cash $"
                    + Money.format(player.getCash()) + "   net worth $" + Money.format(player.getNetWorth()));
        }
    }

    /** The only game operation that changes ownership, for purchases and buy-outs. */
    public void transfer(Ownable square, Player from, Player to, double price) {
        if (to == null || to == from || square.getOwner() != from || !Double.isFinite(price) || price < 0
                || to.getCash() < price) {
            throw new IllegalArgumentException("Invalid or unaffordable ownership transfer.");
        }
        to.addCash(-price);
        if (from != null) {
            from.addCash(price);
            from.removeSquare(square);
        }
        to.addSquare(square);
        square.setOwner(to);
    }

    public void landOnOwnable(Ownable square, Player player) {
        String name = square.getName();
        String group = square.getGroup();
        Player owner = square.getOwner();
        boolean completesGroup = board.countOwned(player, group) == board.getGroupSize(group) - 1;
        String landing = player.getName() + " landed on " + name + " (" + group + "). ";
        if (owner == null) {
            System.out.println(landing + "Unowned, price $" + square.getPrice() + ".");
            if (player.getCash() >= square.getPrice()
                    && (completesGroup || player.wantsToBuy(square))) {
                transfer(square, null, player, square.getPrice());
                System.out.println(player.getName() + " bought " + name + " for $" + square.getPrice() + ".");
            } else {
                System.out.println(player.getName() + " does not buy " + name + ".");
            }
        } else if (owner == player) {
            System.out.println(landing + "Already owned by you.");
        } else {
            double buyoutPrice = square.getPrice() * BUYOUT_FACTOR;
            if (completesGroup && player.getCash() >= buyoutPrice) {
                System.out.println(landing + "Owned by " + owner.getName() + ".");
                transfer(square, owner, player, buyoutPrice);
                System.out.println(player.getName() + " owns the rest of " + group + " and buys "
                        + name + " from " + owner.getName() + " for $" + Money.format(buyoutPrice) + ".");
            } else {
                boolean fullGroup = board.countOwned(owner, group) == board.getGroupSize(group);
                double rent = (double) square.getBaseRent() * (fullGroup ? FULL_GROUP_RENT_FACTOR : 1);
                System.out.println(landing + "Owned by " + owner.getName()
                        + (fullGroup ? " (whole group)" : "") + ". Rent: $" + Money.format(rent) + ".");
                player.addCash(-rent);
                owner.addCash(rent);
                System.out.println(player.getName() + " paid $" + Money.format(rent) + " to " + owner.getName() + ".");
            }
        }
    }
}
