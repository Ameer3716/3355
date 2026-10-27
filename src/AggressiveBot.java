public class AggressiveBot extends Player {
    public AggressiveBot(String name) { super(name); }

    @Override
    public boolean wantsToBuy(Ownable square) {
        return getCash() >= square.getPrice();
    }
}
