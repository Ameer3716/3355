import java.util.Locale;

/** Keep whole-dollar sample output unchanged while showing fractional dollars as cents. */
public final class Money {
    private Money() { }

    public static String format(double amount) {
        return String.format(Locale.ROOT, amount == Math.rint(amount) ? "%.0f" : "%.2f", amount);
    }
}
