/** Shared group names; instance objects are not needed for this utility. */
public final class ColorGroup {
    public static final String BROWN = "BROWN";
    public static final String YELLOW = "YELLOW";
    public static final String GREEN = "GREEN";
    public static final String BLUE = "BLUE";
    public static final String RAILROAD = "RAILROAD";

    private ColorGroup() {
    }

    public static boolean isValid(String group) {
        return BROWN.equals(group) || YELLOW.equals(group)
                || GREEN.equals(group) || BLUE.equals(group)
                || RAILROAD.equals(group);
    }
}
