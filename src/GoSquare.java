
public class GoSquare extends Square {
    public GoSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println(p.getName() + " landed on GO.");
    }
}

