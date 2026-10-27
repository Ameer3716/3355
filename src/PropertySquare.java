
public class PropertySquare extends Square implements Ownable {
    private int price;
    private int baseRent;
    private String group;
    private Player owner;

    public PropertySquare(int index, String name, String group, int price, int baseRent) {
        super(index, name);
        this.group = group;
        this.price = price;
        this.baseRent = baseRent;
        this.owner = null;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getBaseRent() {
        return baseRent;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player p) {
        owner = p;
    }

    @Override
    public void land(Player p, Game g) {
        System.out.println("Landed on " + getName() + ".");
    }

    @Override
    public String toString() {
        return super.toString() + " (" + group + ") price $" + price
                + ", rent $" + baseRent;
    }
}

