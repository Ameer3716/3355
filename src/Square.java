
public abstract class Square {
    private final int index;
    private final String name;

    public Square(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return index + ": " + name;
    }

    public abstract void land(Player p, Game g);
}
