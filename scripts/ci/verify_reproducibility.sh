#!/usr/bin/env sh
# Pragmatic build-reproducibility check.
#
# "Pragmatic" because a byte-for-byte reproducible build of a Java project is
# sensitive to timestamps inside JAR/ZIP entries. This script builds the reactor
# twice into two separate local repositories / output trees and compares the
# *normalized* content of the produced JARs:
#
#   - JAR entries are listed and hashed with their per-entry content digest,
#     ignoring entry timestamps and the JAR manifest fields that legitimately
#     vary (Build-Jdk-Spec, Built-By, Created-By, Build-Time...).
#   - The set of {entry-name -> content-sha256} maps must match between runs.
#
# It relies only on the existing Maven build; it does not modify any POM. If the
# project already sets ``project.build.outputTimestamp`` the raw JARs will also
# match, but this check does not require it.
#
# Usage:
#   scripts/ci/verify_reproducibility.sh [REPO_ROOT]
#
# Requires: mvn, java's jar/unzip is NOT required (uses python3 zipfile).
# Exit codes: 0 reproducible (normalized), 1 mismatch, 2 setup error.
set -eu

REPO_ROOT="${1:-${GITHUB_WORKSPACE:-.}}"
cd "$REPO_ROOT"

if ! command -v mvn >/dev/null 2>&1; then
    echo "error: mvn not found on PATH" >&2
    exit 2
fi
if ! command -v python3 >/dev/null 2>&1; then
    echo "error: python3 not found on PATH" >&2
    exit 2
fi

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

MVN_COMMON="-B -q -DskipTests"

echo "== reproducibility: build #1 =="
mvn $MVN_COMMON clean package
python3 scripts/ci/_jar_fingerprint.py "$REPO_ROOT" > "$WORK/run1.json"

echo "== reproducibility: build #2 =="
mvn $MVN_COMMON clean package
python3 scripts/ci/_jar_fingerprint.py "$REPO_ROOT" > "$WORK/run2.json"

echo "== reproducibility: comparing normalized fingerprints =="
python3 scripts/ci/_jar_fingerprint.py --compare "$WORK/run1.json" "$WORK/run2.json"
