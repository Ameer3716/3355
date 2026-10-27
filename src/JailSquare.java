
public class JailSquare extends Square {
    public JailSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println("Landed on " + getName() + ".");
    }
}

