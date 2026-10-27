# RiyadhPoly — Phase 2: Players and Money

A standalone Java console game, built on the supplied Phase 1 project. No Maven,
Gradle, external libraries, services, API keys, or network connection are needed to play.
The original board and planned whole-project draw.io diagram are preserved.

## Run

Install Java 8 or newer. From the project root:

```sh
java -jar RiyadhPoly.jar
# Or load the instructor's board:
java -jar RiyadhPoly.jar path/to/board.txt
```

Enter a whole-number seed (for example `42`), or press Enter for random play.
Then enter four names and bot types: `C` (cautious) or `A` (aggressive).
Invalid seeds, blank names and unknown bot types are re-prompted; EOF cancels safely.
After setup the game runs all 20 rounds automatically. The same seed AND player
inputs AND board produce exactly the same output. Dice need not match the overview's
illustrative dice values.

Windows: double-click `run.bat`, or use the terminal commands above.
Linux/macOS: `sh run.sh`. These scripts use the packaged JAR; rebuild after edits.

## Build and test

A JDK is required to compile. Java 21 was used for validation. Scripts also support
this cloud image's compiler modules when the `javac` and `jar` launchers are absent.
Source and bytecode target Java 8; execution on Java 8 itself has not been tested.

Linux/macOS:

```sh
sh build.sh
sh test.sh
java -cp build RentTest
# Optional extra CLI checks; requires Python 3:
python3 tests/verify_cli.py
```

Windows: `build.bat`, `test.bat`, then `java -cp build RentTest`.
The Windows scripts have been updated but were not executed on Windows here.
To work in an IDE, use `src` as the source directory, no package, and the project
root as the working directory. Run `RiyadhPolyApp.main` or `RentTest.main`.

## Rules and design

- `Player` is abstract, implements `Comparable<Player>`, and holds name, $800 cash,
  GO position, an owned-square list, and `turnsInJail` for Phase 3.
- `AggressiveBot.wantsToBuy` buys whenever affordable. `CautiousBot.wantsToBuy`
  leaves at least $300 for ordinary purchases. `Game` overrides that reserve when
  an affordable normal purchase or buy-out completes a group.
- `Dice` uses one `Random` created at startup, rolling two six-sided dice in order.
- Each player moves forward by the sum modulo 20 and gets $100 on passing or
  landing on GO. `Square.land` dispatches the landing polymorphically.
- `PropertySquare` and `StationSquare` delegate to the same `Game.landOnOwnable`.
  Unowned squares use normal prices; your own squares cost nothing. An opponent's
  square is bought out at 1.5 times its price only if you own every other square
  in its group and can afford the buy-out. Otherwise you pay the owner rent.
- A full group doubles board-file base rent. Stations are the two-member RAILROAD
  group: $100 base rent, $200 full-group rent; station buy-out costs $450.
- All purchases and buy-outs use `Game.transfer` to update cash, the square owner,
  and BOTH ownership lists. Net worth values squares at their original purchase
  price, even after a buy-out.
- After each round a COPY of the player list is sorted with `Collections.sort`:
  net worth descending, cash descending, then names ascending. Turn order stays
  in creation order. Change only `Game.NUM_PLAYERS` to change the player count.

Phase 2 intentionally leaves Chance and Go To Jail as printed placeholders. GO,
Park and Jail print messages (Jail is just visiting). No Chance deck, jail penalty,
bankruptcy elimination or final winner is implemented: the phase brief assigns
these to Phase 3. Negative cash is allowed and every player keeps taking turns for
all 20 rounds in this phase.

All supplied board prices are even, so buy-out amounts are exact integers. For an
alternate board with odd prices, the integer-cash implementation truncates a half
dollar buy-out down; the brief does not specify fractional-dollar rounding.

## Check-in and evidence

1. Run `java -jar RiyadhPoly.jar` with your instructor's seed and four players.
2. Run `java -cp build RentTest` after a build. Expect Al-Oud's owner to become Amal
   at $180, then $96 rent for Al-Bathaa and Sara's cash of **$764**. The test fails
   explicitly if the amounts or ownership lists are wrong; `-ea` is not needed.
3. Read [the requirement checklist](docs/phase2_requirements.md) and
   [the code explanation](docs/phase2_check_in.md) before the check-in.

`sh test.sh` runs 33 Phase 1 regression checks, 31 Phase 2 checks and RentTest.
The five extra CLI checks run the packaged JAR twice, check all 80 turns and their
order, exercise invalid input and board failures, and compile a temporary version
with ONLY `NUM_PLAYERS` changed to 2 to verify all 40 turns. They leave source intact.

Actual results and a 20-round seed-42 game are in `docs/phase2_*output.txt` and
`docs/phase2_test_results.txt`. Prompts do not echo redirected input; the sample
uses seed 42, Amal/A, Sara/C, Noura/C, Huda/A. Original `docs/` files without the
`phase2_` prefix are historical Phase 1 evidence.

The supplied draw.io diagram includes planned Phase 3 classes. See
[the current Phase 2 diagram](diagram/phase2.md) for implemented classes only.
The source ZIP had no separate PNG diagram or LMS sample-output file.

## AI log and submission responsibilities

[docs/AI_USE_LOG.md](docs/AI_USE_LOG.md) records this AI-assisted implementation.
The group must fill in its actual student names, honestly record its own review,
changes and learning, and understand the submitted code. No student review or
learning is claimed by the generated log. LMS submission, the three-person group,
attendance and individual check-in/viva answers must be completed by the students.
The code and documentation cannot guarantee marks or satisfy those personal tasks.
