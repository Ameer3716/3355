/** Phase 1 landing behavior only; full rules are added in later phases. */
public class ChanceSquare extends Square {
    public ChanceSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println("Landed on " + getName() + ".");
    }
}

