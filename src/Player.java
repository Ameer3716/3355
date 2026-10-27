import java.util.ArrayList;

/** Shared player state; subclasses decide only whether they want a normal purchase. */
public abstract class Player implements Comparable<Player> {
    public static final int INITIAL_CASH = 800;
    private final String name;
    private int cash = INITIAL_CASH;
    private int position;
    private int turnsInJail;
    private final ArrayList<Ownable> ownedSquares = new ArrayList<>();

    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name must not be empty.");
        }
        this.name = name.trim();
    }

    public String getName() { return name; }
    public int getCash() { return cash; }
    public void addCash(int amount) { cash += amount; }
    public int getPosition() { return position; }
    public void setPosition(int position) {
        if (position < 0 || position >= Board.BOARD_SIZE) {
            throw new IllegalArgumentException("Position outside board.");
        }
        this.position = position;
    }
    public int getTurnsInJail() { return turnsInJail; }
    public void setTurnsInJail(int turns) {
        if (turns < 0) { throw new IllegalArgumentException("Negative jail turns."); }
        turnsInJail = turns;
    }
    public ArrayList<Ownable> getOwnedSquares() { return new ArrayList<>(ownedSquares); }
    public void addSquare(Ownable square) {
        if (!ownedSquares.contains(square)) { ownedSquares.add(square); }
    }
    public void removeSquare(Ownable square) { ownedSquares.remove(square); }
    public int getNetWorth() {
        int worth = cash;
        for (Ownable square : ownedSquares) { worth += square.getPrice(); }
        return worth;
    }
    public abstract boolean wantsToBuy(Ownable square);

    @Override
    public int compareTo(Player other) {
        int comparison = Integer.compare(other.getNetWorth(), getNetWorth());
        if (comparison == 0) { comparison = Integer.compare(other.cash, cash); }
        return comparison == 0 ? name.compareTo(other.name) : comparison;
    }
}
