#!/usr/bin/env python3
"""Normalized JAR fingerprinting for pragmatic reproducibility checks.

Two modes:

  * Default (scan): given a repo root, find every ``*/target/*.jar`` produced by
    the reactor and emit a JSON map of ``{jar-relative-path: {entry: sha256}}``.
    Entry timestamps are ignored (only entry *content* is hashed), and volatile
    ``META-INF/MANIFEST.MF`` lines (Build-Jdk-Spec, Built-By, Created-By,
    Build-Time, Bnd-LastModified, etc.) are stripped before hashing so that
    manifests differing only by build metadata still compare equal.

  * ``--compare A.json B.json``: compare two previously captured fingerprints and
    exit non-zero if any normalized JAR content differs.

Standard library only (uses ``zipfile``); no external tools.
"""
from __future__ import annotations

import hashlib
import json
import sys
import zipfile
from pathlib import Path

VOLATILE_MANIFEST_KEYS = {
    "build-jdk", "build-jdk-spec", "built-by", "created-by", "build-time",
    "bnd-lastmodified", "bundle-version", "implementation-build",
}


def _normalize_manifest(data: bytes) -> bytes:
    lines = data.split(b"\n")
    kept = []
    for line in lines:
        key = line.split(b":", 1)[0].strip().lower().decode("latin-1", "replace")
        if key in VOLATILE_MANIFEST_KEYS:
            continue
        kept.append(line)
    return b"\n".join(kept)


def fingerprint_jar(path: Path) -> dict[str, str]:
    entries: dict[str, str] = {}
    with zipfile.ZipFile(path) as zf:
        for info in sorted(zf.infolist(), key=lambda i: i.filename):
            if info.is_dir():
                continue
            content = zf.read(info.filename)
            if info.filename.upper() == "META-INF/MANIFEST.MF":
                content = _normalize_manifest(content)
            entries[info.filename] = hashlib.sha256(content).hexdigest()
    return entries


def scan(repo_root: Path) -> dict:
    result: dict[str, dict[str, str]] = {}
    for jar in sorted(repo_root.glob("*/target/*.jar")):
        # skip sources/javadoc which are not part of the runtime artifact set
        if jar.name.endswith(("-sources.jar", "-javadoc.jar")):
            continue
        rel = str(jar.relative_to(repo_root))
        try:
            result[rel] = fingerprint_jar(jar)
        except zipfile.BadZipFile:
            result[rel] = {"__error__": "bad-zip"}
    return result


def compare(a_path: Path, b_path: Path) -> int:
    a = json.loads(a_path.read_text(encoding="utf-8"))
    b = json.loads(b_path.read_text(encoding="utf-8"))

    a_jars = set(a) & {k for k in a}
    b_jars = set(b)
    diffs = []

    only_a = sorted(set(a) - b_jars)
    only_b = sorted(b_jars - set(a))
    for j in only_a:
        diffs.append(f"jar only in run1: {j}")
    for j in only_b:
        diffs.append(f"jar only in run2: {j}")

    for jar in sorted(set(a) & b_jars):
        ea, eb = a[jar], b[jar]
        for entry in sorted(set(ea) - set(eb)):
            diffs.append(f"{jar}: entry only in run1: {entry}")
        for entry in sorted(set(eb) - set(ea)):
            diffs.append(f"{jar}: entry only in run2: {entry}")
        for entry in sorted(set(ea) & set(eb)):
            if ea[entry] != eb[entry]:
                diffs.append(f"{jar}: content differs: {entry}")

    if diffs:
        print("REPRODUCIBILITY: MISMATCH")
        for d in diffs:
            print(f"  - {d}")
        return 1
    total = sum(len(v) for v in a.values())
    print(f"REPRODUCIBILITY: OK ({len(a)} jars, {total} normalized entries match)")
    return 0


def main(argv: list[str] | None = None) -> int:
    argv = list(sys.argv[1:] if argv is None else argv)
    if argv and argv[0] == "--compare":
        if len(argv) != 3:
            print("usage: _jar_fingerprint.py --compare A.json B.json", file=sys.stderr)
            return 2
        return compare(Path(argv[1]), Path(argv[2]))

    repo_root = Path(argv[0] if argv else ".").resolve()
    result = scan(repo_root)
    json.dump(result, sys.stdout, indent=2, sort_keys=True)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
