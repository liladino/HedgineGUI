
import argparse
import io
import os
import random
from collections import Counter

import chess
import chess.pgn

# python .\openings.py "lichess_db_standard_rated_2015-09.pgn" --plies 10 --min-occurrences 2 --output fens.txt

def open_pgn(path):
    if path.lower().endswith(".zst"):
        import zstandard

        raw = open(path, "rb")
        stream = zstandard.ZstdDecompressor().stream_reader(raw)

        return io.TextIOWrapper(stream, encoding="utf-8")

    return open(path, "r", encoding="utf-8")


def headers_match(headers, args, speeds):
    # Standard chess, normal starting position only.
    if headers.get("Variant", "Standard") != "Standard":
        return False

    if headers.get("FEN"):
        return False

    # Both players must meet the Elo threshold.
    try:
        white_elo = int(headers.get("WhiteElo", "0"))
        black_elo = int(headers.get("BlackElo", "0"))
    except ValueError:
        return False

    if min(white_elo, black_elo) < args.min_elo:
        return False

    # Optional speed filtering.
    if speeds is not None:
        event = headers.get("Event", "").lower().split()
        if not any(speed in event for speed in speeds):
            return False

    # Optional exact date filtering.
    if args.since or args.until:
        date = headers.get(
            "UTCDate", headers.get("Date", "")
        ).replace(".", "-")

        if len(date) != 10 or "?" in date:
            return False

        if args.since and date < args.since:
            return False

        if args.until and date > args.until:
            return False

    return True


class OpeningVisitor(chess.pgn.BaseVisitor):
    """
    Reads only what is needed: games rejected by their headers are
    skipped without parsing moves, and accepted games stop parsing
    SAN once the target ply is reached. Comments are ignored.
    """

    def __init__(self, args, speeds, target_ply):
        self.args = args
        self.speeds = speeds
        self.target_ply = target_ply

        self.headers = {}
        self.ply = 0
        self.fen = None

    def visit_header(self, tagname, tagvalue):
        self.headers[tagname] = tagvalue

    def end_headers(self):
        if not headers_match(self.headers, self.args, self.speeds):
            return chess.pgn.SKIP

    def begin_parse_san(self, board, san):
        if self.ply >= self.target_ply:
            return chess.pgn.SKIP

    def visit_move(self, board, move):
        self.ply += 1

    def visit_board(self, board):
        if self.ply == self.target_ply and self.fen is None:
            self.fen = board.fen()

    def handle_error(self, error):
        # The parser stops reading the mainline after an illegal
        # move, so the target ply is never reached.
        pass

    def result(self):
        return self


def extract(args):
    rng = random.Random(args.seed)
    target_ply = args.plies

    counts = Counter()
    full_fens = {}

    scanned = 0
    accepted = 0

    speeds = (
        {s.strip().lower() for s in args.speeds.split(",")}
        if args.speeds else None
    )

    def make_visitor():
        return OpeningVisitor(args, speeds, target_ply)

    with open_pgn(args.input) as pgn:
        while args.max_games == 0 or scanned < args.max_games:
            game = chess.pgn.read_game(pgn, Visitor=make_visitor)

            if game is None:
                break

            scanned += 1

            if game.fen is not None:
                full_fen = game.fen

                # Deduplicate by board position, ignoring
                # the halfmove and fullmove counters.
                key = " ".join(full_fen.split()[:4])

                counts[key] += 1

                if key not in full_fens:
                    full_fens[key] = full_fen

                accepted += 1

            if scanned % 100_000 == 0:
                print(
                    f"Scanned: {scanned:,} | "
                    f"Accepted: {accepted:,} | "
                    f"Unique: {len(counts):,}",
                    flush=True
                )

    # Positions already present in the output file are kept and
    # excluded from the new selection to avoid duplicates.
    existing = set()
    needs_newline = False

    if os.path.exists(args.output):
        with open(args.output, "r", encoding="utf-8") as f:
            content = f.read()

        needs_newline = bool(content) and not content.endswith("\n")

        for line in content.splitlines():
            if line.strip():
                existing.add(" ".join(line.split()[:4]))

    candidates = [
        key for key, count in counts.items()
        if count >= args.min_occurrences and key not in existing
    ]

    selected = rng.sample(
        candidates,
        min(args.max_positions, len(candidates))
    )

    with open(args.output, "a", encoding="utf-8") as f:
        if needs_newline:
            f.write("\n")

        for key in selected:
            f.write(full_fens[key] + "\n")

    print()
    print(f"Games scanned:      {scanned:,}")
    print(f"Games accepted:     {accepted:,}")
    print(f"Unique positions:   {len(counts):,}")
    print(f"Eligible positions: {len(candidates):,}")
    print(f"Already in output:  {len(existing):,}")
    print(f"Exported:           {len(selected):,}")
    print(f"Output:             {args.output}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument("input", help="Input .pgn or .pgn.zst")
    parser.add_argument("-o", "--output", default="openings.txt")

    parser.add_argument("-m", "--plies", type=int, default=10)
    parser.add_argument("-n", "--max-positions", type=int, default=2000)
    parser.add_argument("--min-occurrences", type=int, default=5)
    parser.add_argument("--min-elo", type=int, default=2100)

    parser.add_argument(
        "--speeds",
        default="blitz,rapid,classical"
    )

    parser.add_argument("--since", help="YYYY-MM-DD")
    parser.add_argument("--until", help="YYYY-MM-DD")

    parser.add_argument(
        "--max-games",
        type=int,
        default=500_000,
        help="Maximum games to scan; 0 means all"
    )

    parser.add_argument("--seed", type=int, default=42)

    args = parser.parse_args()

    if args.plies < 1 or args.max_positions < 1:
        parser.error("--plies and --max-positions must be positive")
    if args.min_occurrences < 1 or args.max_games < 0:
        parser.error("Invalid occurrences or max-games value")

    extract(args)
