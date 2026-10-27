/** Phase 2 placeholder; Chance cards arrive in Phase 3. */
public class ChanceSquare extends Square {
    public ChanceSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println(p.getName() + " landed on Chance. Cards arrive in Phase 3.");
    }
}

