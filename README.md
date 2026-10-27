# RiyadhPoly - Phase 1: Design and Board

This is a standalone Java project with no Maven, Gradle or external libraries.
The supplied `board.txt` and both supplied diagram files are included unchanged.

## Run on Windows

1. Extract the complete ZIP into a folder.
2. Make sure Java 8 or newer is installed and available on PATH.
3. Open the extracted `RiyadhPoly_Phase1_Project` folder and double-click `run.bat`.

The included `RiyadhPoly.jar` is already compiled. A JDK is needed only to rebuild,
run the source tests, or work on the Java files in an IDE.

From a terminal opened in the project folder:

```text
java -jar RiyadhPoly.jar
```

To load another board file:

```text
java -jar RiyadhPoly.jar "C:\path\to\another-board.txt"
```

On macOS or Linux, run `sh run.sh` from the extracted project folder.

## Rebuild and test

Windows: run `build.bat` to rebuild, or `test.bat` for the acceptance checks.
macOS/Linux: run `sh build.sh` or `sh test.sh`.

Standard JDK commands, without the scripts:

```text
mkdir build
javac -encoding UTF-8 -d build src/*.java
java -cp build RiyadhPolyApp
```

Open the `src` folder in your IDE, add its files to a Java project, and run
`RiyadhPolyApp.main`. Set the working directory to the project root so that
`board.txt` can be found. You can also pass a board filename as an argument.

## What Phase 1 implements

- `Square` is abstract and its seven subclasses override `land(Player, Game)`.
- `PropertySquare` and `StationSquare` implement `Ownable`; owners start as null.
- `ColorGroup` declares all five public static final group names.
- `Board` loads 20 ordered squares from a six-field CSV text file and builds
  `Map<String, ArrayList<Ownable>>` while loading.
- Comments and empty lines are skipped. Trailing empty CSV fields are preserved.
- `InvalidBoardException` is checked and retains causes for I/O and numeric errors.
- Invalid input prints one clear error and stops with exit code 1.
- `Player` and `Game` are empty stubs, exactly as requested for this phase.
- `countOwned` returns 0 for every group while the squares are unowned.

The whole-project diagram deliberately includes future classes and planned members.
Bots, Comparable, the game loop, dice, money and RentTest belong to Phase 2;
chance cards, jail and bankruptcy belong to Phase 3. They are not implemented here.
`Phase1Tests` is a separate acceptance-test runner, not the future `RentTest` class.
Small validation helpers such as `ColorGroup.isValid` and `Board.parseNumber` are
implementation helpers permitted by the brief.

The loader checks the required errors and also rejects missing names, incorrect
index order/count, negative prices/rents, or non-empty monetary fields on special
squares. Properties use the four colour groups and stations use RAILROAD.
It does not hard-code square names, positions by square type, prices, rents, or
the number of properties in each group.

## Check-in demonstration

Run the valid supplied board, then demonstrate the three required invalid cases:

```text
java -jar RiyadhPoly.jar tests/fixtures/invalid_missing_field.txt
java -jar RiyadhPoly.jar tests/fixtures/invalid_unknown_group.txt
java -jar RiyadhPoly.jar tests/fixtures/invalid_non_numeric_price.txt
```

These intentionally fail and should print a clear error. The fixtures preserve
physical line numbers, including the comment line at the top. An error therefore
reports the actual line in the file, not the square index.

Also run your instructor's separately assigned board file if it differs from the
file supplied for this project. Automated tests additionally change a board name,
price and rent to check that the loader uses file contents rather than fixed data.

## Files

- `src/`: 15 Java source files for the Phase 1 application.
- `board.txt`: the exact board file supplied by the user.
- `RiyadhPoly.jar`: ready-to-run application.
- `diagram/`: the supplied whole-project draw.io file and PNG export.
- `tests/`: acceptance tests and three intentionally invalid board fixtures.
- `docs/`: captured output and verification results from the actual runs.
- `run`, `build`, and `test` scripts for Windows and macOS/Linux.

No board data is embedded in the JAR. Keep `board.txt` beside it, or pass another
file explicitly. After editing Java source, run the build script to update the JAR.
