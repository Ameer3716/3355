import java.util.ArrayList;

/** Shared player state; subclasses decide only whether they want a normal purchase. */
public abstract class Player implements Comparable<Player> {
    public static final int INITIAL_CASH = 800;
    private final String name;
    // Integer board prices and 1.5x buy-outs produce only whole/half dollars.
    // Those values are exactly representable as doubles at this game's scale.
    private double cash = INITIAL_CASH;
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
    public double getCash() { return cash; }
    public void addCash(double amount) {
        if (!Double.isFinite(amount) || !Double.isFinite(cash + amount)) {
            throw new IllegalArgumentException("Cash adjustment must be finite.");
        }
        cash += amount;
    }
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
    public double getNetWorth() {
        double worth = cash;
        for (Ownable square : ownedSquares) { worth += square.getPrice(); }
        return worth;
    }
    public abstract boolean wantsToBuy(Ownable square);

    @Override
    public int compareTo(Player other) {
        int comparison = Double.compare(other.getNetWorth(), getNetWorth());
        if (comparison == 0) { comparison = Double.compare(other.cash, cash); }
        return comparison == 0 ? name.compareTo(other.name) : comparison;
    }
}
