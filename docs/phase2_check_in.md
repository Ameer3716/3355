# Understand and demonstrate Phase 2

Run `sh test.sh` (Windows: `test.bat`). Run the game with the instructor's seed, then
`java -cp build RentTest`. Use the required board file and project-root working directory.

- **Why abstract Player?** Shared state belongs in Player, but each concrete bot
  supplies its own `wantsToBuy`. Game asks a Player that question without checking
  which bot subclass it has. Startup alone constructs the chosen concrete type.
- **Where is buy-out decided?** In `Game.landOnOwnable`, only after finding that a
  different player owns the square. The visitor must own `groupSize - 1` other
  squares and have the 1.5-times price. No rent is charged in that branch.
- **Why not put group completion in CautiousBot?** Group completion is a board rule
  that applies to every bot. Game checks it before asking the ordinary bot policy.
- **How is ownership kept consistent?** Every purchase/buy-out calls `transfer`.
  It debits the buyer, credits/removes from the seller when present, adds the
  square to the buyer's list, then changes the square's owner. Null seller is bank.
- **Why a single Random?** Dice takes the startup Random and consumes exactly two
  values per turn. Recreating it on each roll would destroy a repeatable sequence.
- **Why sort a copy?** Sorting the live player list would silently change the next
  round's turn order. Comparable sorts worth first, cash second, names last.
- **What is net worth?** Cash plus board-file purchase prices, not the amounts paid
  in buy-outs. Moving cash to a normal purchase preserves net worth.
- **Why does GO not pay in land?** Movement already pays on wrapping past or onto
  square zero. Paying again in GoSquare would double the salary when landing on GO.
- **Why can cash be negative?** Phase 2 always gives all players 20 turns.
  Elimination and returning assets to the bank belong to Phase 3.
- **RentTest arithmetic:** Amal: 800 - 100 - 100 - 180 = 420. Sara:
  800 - 120 + 180 = 860. Brown rent on Al-Bathaa: 48 × 2 = 96.
  After rent: Amal 516, Sara 764. Amal holds all three browns; Sara holds none.

Practice changing BOT_RESERVE, SALARY, or NUM_PLAYERS, rebuilding, and explaining
which behavior changes. Read every implementation before submitting it; this guide
is preparation material, not evidence that any student has understood the code.
