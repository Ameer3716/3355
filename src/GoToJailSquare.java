
public class GoToJailSquare extends Square {
    public GoToJailSquare(int index, String name) {
        super(index, name);
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println(p.getName() + " landed on Go To Jail. Jail penalties arrive in Phase 3.");
    }
}

