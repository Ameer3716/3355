import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Loads the ordered board and indexes ownable squares by their groups. */
public class Board {
    public static final int BOARD_SIZE = 20;
    public static final int CSV_FIELD_COUNT = 6;

    private ArrayList<Square> squares = new ArrayList<>();
    private Map<String, ArrayList<Ownable>> groups = new HashMap<>();

    public Board(String fileName) throws InvalidBoardException {
        loadBoard(fileName);
    }

    public ArrayList<Square> getSquares() {
       
        return new ArrayList<>(squares);
    }

    public Square getSquare(int index) {
        return squares.get(index);
    }

    public int getGroupSize(String group) {
        ArrayList<Ownable> members = groups.get(group);
        return members == null ? 0 : members.size();
    }

    public int countOwned(Player p, String group) {
        
        if (p == null || !groups.containsKey(group)) {
            return 0;
        }
        int count = 0;
        for (Ownable square : groups.get(group)) {
            if (square.getOwner() == p) {
                count++;
            }
        }
        return count;
    }

    public void printBoard() {
        for (Square square : squares) {
            System.out.println(square);
        }
    }

    private void loadBoard(String fileName) throws InvalidBoardException {
        int lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get(fileName), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Some text editors put a UTF-8 byte-order mark on the first line.
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (squares.size() >= BOARD_SIZE) {
                    throw new InvalidBoardException(lineNumber,
                            "too many squares; expected " + BOARD_SIZE);
                }

                // -1 preserves all trailing empty fields on special squares.
                String[] fields = line.split(",", -1);
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim();
                }
                validateRecord(fields, lineNumber);
                Square square = parseSquare(fields);
                squares.add(square);

                if (square instanceof Ownable) {
                    Ownable ownable = (Ownable) square;
                    groups.putIfAbsent(ownable.getGroup(), new ArrayList<Ownable>());
                    groups.get(ownable.getGroup()).add(ownable);
                }
            }
        } catch (IOException | InvalidPathException e) {
            InvalidBoardException failure = new InvalidBoardException(
                    "cannot read board file: " + e.getMessage());
            failure.initCause(e);
            throw failure;
        }

        if (squares.size() != BOARD_SIZE) {
            throw new InvalidBoardException("expected " + BOARD_SIZE
                    + " squares but found " + squares.size());
        }
    }

    private void validateRecord(String[] fields, int lineNumber)
            throws InvalidBoardException {
        if (fields.length != CSV_FIELD_COUNT) {
            throw new InvalidBoardException(lineNumber, "expected "
                    + CSV_FIELD_COUNT + " fields but found " + fields.length);
        }

        int index = parseNumber(fields[0], "index", lineNumber);
        if (index != squares.size()) {
            throw new InvalidBoardException(lineNumber,
                    "expected square index " + squares.size() + " but found " + index);
        }
        if (fields[2].isEmpty()) {
            throw new InvalidBoardException(lineNumber, "square name is empty");
        }

        String type = fields[1];
        switch (type) {
            case "GO":
            case "CHANCE":
            case "PARK":
            case "JAIL":
            case "GO_TO_JAIL":
                if (!fields[3].isEmpty() || !fields[4].isEmpty()
                        || !fields[5].isEmpty()) {
                    throw new InvalidBoardException(lineNumber,
                            "special squares must leave group, price and rent empty");
                }
                break;
            case "PROPERTY":
            case "STATION":
                if (!ColorGroup.isValid(fields[3])) {
                    throw new InvalidBoardException(lineNumber,
                            "unknown group \"" + fields[3] + "\"");
                }
                if ("STATION".equals(type) && !ColorGroup.RAILROAD.equals(fields[3])) {
                    throw new InvalidBoardException(lineNumber,
                            "a station must use the RAILROAD group");
                }
                if ("PROPERTY".equals(type) && ColorGroup.RAILROAD.equals(fields[3])) {
                    throw new InvalidBoardException(lineNumber,
                            "a property must use BROWN, YELLOW, GREEN or BLUE");
                }
                int price = parseNumber(fields[4], "price", lineNumber);
                int rent = parseNumber(fields[5], "rent", lineNumber);
                if (price < 0 || rent < 0) {
                    throw new InvalidBoardException(lineNumber,
                            "price and rent must not be negative");
                }
                break;
            default:
                throw new InvalidBoardException(lineNumber,
                        "unknown type \"" + type + "\"");
        }
    }

    private int parseNumber(String value, String fieldName, int lineNumber)
            throws InvalidBoardException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            InvalidBoardException failure = new InvalidBoardException(lineNumber,
                    fieldName + " \"" + value + "\" is not a valid integer");
            failure.initCause(e);
            throw failure;
        }
    }

    private Square parseSquare(String[] fields) {
        int index = Integer.parseInt(fields[0]);
        String name = fields[2];
        // The type and numeric values have already passed validateRecord.
        switch (fields[1]) {
            case "GO":
                return new GoSquare(index, name);
            case "CHANCE":
                return new ChanceSquare(index, name);
            case "PARK":
                return new ParkSquare(index, name);
            case "JAIL":
                return new JailSquare(index, name);
            case "GO_TO_JAIL":
                return new GoToJailSquare(index, name);
            case "PROPERTY":
                return new PropertySquare(index, name, fields[3],
                        Integer.parseInt(fields[4]), Integer.parseInt(fields[5]));
            case "STATION":
                return new StationSquare(index, name, fields[3],
                        Integer.parseInt(fields[4]), Integer.parseInt(fields[5]));
            default:
                throw new IllegalStateException("A validated type was not handled");
        }
    }
}
