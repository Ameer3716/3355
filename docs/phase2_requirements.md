# Phase 2 requirement checklist

Sources: supplied Overview and “Phase 2 — Players and Money 2”. Phase 2 explicitly
supersedes the overview's final-game behavior for Chance, jail and bankruptcy.

| Requirement | Implementation / evidence |
| --- | --- |
| Build on Phase 1, Java, board loaded from file | Original import commit; `Board`, unchanged `board.txt`; 33 board regression checks |
| Square abstract, 7 subclasses, Ownable interface | Preserved; property/station landing delegates through shared helper |
| ArrayList board/players/ownership; Map group index | `Board`, `Game`, `Player` |
| Abstract Player with all required state, Comparable | `Player.java`; position/jail start at zero; cash $800 |
| Two polymorphic bot strategies | `AggressiveBot`, `CautiousBot`; ordinary purchase affordability/reserve boundary tests |
| Named constants | `NUM_PLAYERS`, `INITIAL_CASH`, `SALARY`, `MAX_ROUNDS`, `BOT_RESERVE`, `BUYOUT_FACTOR`; board/dice/rent constants |
| 4 players by default in creation order | Startup loops on `NUM_PLAYERS`; leaderboard sorts a copy; CLI verifies every turn |
| One seeded Random, 2 six-sided dice | `Dice`, `Game.startup`; 1,000 paired rolls checked against independent Random |
| Blank seed uses random; numeric seed reproducible | Scanner input, Long parsing; two complete outputs compared byte for byte |
| Invalid seed/type/name safely re-prompted | Unit and packaged-CLI tests; overflow and EOF also checked |
| Move modulo BOARD_SIZE; print both dice and sum | `takeTurn`; movement tests and complete CLI game |
| Salary on passing AND landing exactly on GO | `takeTurn`; targeted seed-42 movement from 16 and 13 |
| Polymorphic land(player, game) | `takeTurn` calls Square, without property/station subclass checks |
| Unowned normal buying; own square no cost | `landOnOwnable`; affordable/declined and own-square tests |
| Group completion ignores reserve, requires funds | Property and station last-unowned purchase tests |
| Opponent rent transferred; full-group doubled | Property and station tests, RentTest |
| Buy-out only opponent-owned last group square | `countOwned == getGroupSize - 1` and affordability; $180/$450 tests |
| Buy-out instead of rent, seller paid, both lists updated | `transfer`; RentTest and cautious exact-funds test |
| Cannot afford buy-out: rent instead | $179 brown-completion test pays $56 base rent |
| One method for every purchase/buy-out | `Game.transfer`; only game call to `setOwner` |
| GO/Park/Jail messages; Chance/Go To Jail placeholders | Special-square tests check no money/movement/jail effects |
| Exactly 20 rounds, every player every round | `play`; 80-turn normal and 40-turn two-player CLI checks |
| Net worth = cash + original square prices | `Player.getNetWorth`; no buy-out-price inflation |
| Collections.sort and tie breaks | `compareTo`: worth descending, cash descending, name ascending; sort tests |
| Standalone RentTest main | `src/RentTest.java`: owner Amal; $180 buy-out; $96 rent; Sara $764 |
| Changing only NUM_PLAYERS to 2 works | `tests/verify_cli.py` changes a temporary source copy and runs full game |
| Sample messages and money | Overview-style turns, buying, rent, buy-out and leaderboard; captured run. Separate LMS sample was not supplied |
| AI-use log and explainable code | `AI_USE_LOG.md` and `phase2_check_in.md`; student review/understanding remains required |

## Not part of Phase 2

ChanceCard hierarchy/deck, Iterator bankruptcy removal, jail turn skipping, final
standings/winner are Phase 3 deliverables. They are intentionally not implemented.
No claim is made that the final-project rubric is complete at Phase 2.

## Human requirements

Exactly three students, LMS submission before the Phase 2 deadline (31 October
2026), the lab check-in, and each member's individual explanation remain group
responsibilities. The Overview lists the final submission and viva separately.
