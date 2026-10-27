"""Optional end-to-end checks: python3 tests/verify_cli.py (after sh build.sh)."""
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def run(command, data=None, cwd=ROOT, expected=0):
    result = subprocess.run(command, input=data, text=True, capture_output=True,
                            cwd=cwd, timeout=30)
    assert result.returncode == expected, result.stdout + result.stderr
    return result.stdout + result.stderr


def inputs(count, seed="42"):
    return seed + "\n" + "".join(f"Player{i}\n{'A' if i % 2 else 'C'}\n"
                                  for i in range(1, count + 1))


def full_game(command, count, cwd=ROOT):
    output = run(command, inputs(count), cwd)
    assert output == run(command, inputs(count), cwd)
    assert output.count("========== ROUND ") == 20
    assert output.count(" LEADERBOARD ----------") == 20
    assert output.count("'s turn. Rolled") == count * 20
    # Check every round's playing order independently of the leaderboard.
    for section in output.split("========== ROUND ")[1:]:
        turns = [line.split("'s turn.")[0] for line in section.splitlines()
                 if "'s turn. Rolled" in line]
        assert turns == [f"Player{i}" for i in range(1, count + 1)]
    return output


output = full_game(["java", "-jar", "RiyadhPoly.jar"], 4)
print("PASS: packaged JAR, 20 rounds, 80 turns, stable order, identical seeded output")
invalid = run(["java", "-jar", "RiyadhPoly.jar"],
              "oops\n42\n\nFirst\nunknown\na\nSecond\nc\nThird\nC\nFourth\nA\n")
assert "whole-number seed" in invalid and "Please enter C or A" in invalid
assert "non-empty name" in invalid and "ROUND 20 LEADERBOARD" in invalid
print("PASS: packaged JAR recovers from invalid input and completes the game")
for fixture in (ROOT / "tests/fixtures").glob("*.txt"):
    assert "Error loading" in run(["java", "-jar", "RiyadhPoly.jar", str(fixture)], expected=1)
print("PASS: packaged JAR rejects all three malformed boards with exit status 1")
assert "game cancelled" in run(["java", "-jar", "RiyadhPoly.jar"], "")
print("PASS: packaged JAR handles end of input")

# Change ONLY NUM_PLAYERS in an isolated temporary source copy, never the checkout.
with tempfile.TemporaryDirectory(prefix="riyadhpoly-two-") as directory:
    variant = Path(directory)
    shutil.copytree(ROOT / "src", variant / "src")
    shutil.copy(ROOT / "board.txt", variant / "board.txt")
    game = variant / "src/Game.java"
    original = game.read_text()
    assert original.count("NUM_PLAYERS = 4;") == 1
    game.write_text(original.replace("NUM_PLAYERS = 4;", "NUM_PLAYERS = 2;"))
    (variant / "build").mkdir()
    compiler = (["javac"] if shutil.which("javac") else
                ["java", "-m", "jdk.compiler/com.sun.tools.javac.Main"])
    run(compiler + ["-source", "8", "-target", "8", "-Xlint:-options", "-encoding", "UTF-8", "-d", "build"]
        + [str(p) for p in sorted((variant / "src").glob("*.java"))], cwd=variant)
    full_game(["java", "-cp", "build", "RiyadhPolyApp"], 2, variant)
print("PASS: changing only NUM_PLAYERS to 2 gives 20 rounds and 40 turns")
print("All 5 CLI checks passed.")
