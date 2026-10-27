
public interface Ownable {
    int getPrice();
    int getBaseRent();
    String getGroup();
    Player getOwner();
    void setOwner(Player p);
}
