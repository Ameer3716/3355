
public interface Ownable {
    String getName();
    int getPrice();
    int getBaseRent();
    String getGroup();
    Player getOwner();
    void setOwner(Player p);
}
