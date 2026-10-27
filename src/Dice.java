import java.util.Random;

/** Both dice always use the same Random supplied at startup. */
public class Dice {
    public static final int SIDES = 6;
    private final Random random;
    private int first;
    private int second;

    public Dice(Random random) { this.random = random; }
    public int roll() {
        first = random.nextInt(SIDES) + 1;
        second = random.nextInt(SIDES) + 1;
        return first + second;
    }
    public int getFirst() { return first; }
    public int getSecond() { return second; }
}
