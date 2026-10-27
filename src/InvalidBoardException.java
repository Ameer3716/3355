
public class InvalidBoardException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidBoardException(String message) {
        super(message);
    }

    public InvalidBoardException(int lineNumber, String reason) {
        super("line " + lineNumber + ": " + reason);
    }
}
