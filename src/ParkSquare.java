
public class ParkSquare extends Square {
    public ParkSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println("Landed on " + getName() + ".");
    }
}

