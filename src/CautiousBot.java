public class CautiousBot extends Player {
    public static final int BOT_RESERVE = 300;

    public CautiousBot(String name) { super(name); }

    @Override
    public boolean wantsToBuy(Ownable square) {
        return getCash() - square.getPrice() >= BOT_RESERVE;
    }
}
