#!/usr/bin/env sh
# Generate SHA-256 checksums and a minimal build-provenance document for the
# build artifacts produced by the Maven reactor.
#
# It scans the reactor for produced JARs and the aggregate CycloneDX SBOM,
# writes a sorted SHA-256SUMS file, and emits a provenance JSON capturing the
# build environment (commit, workflow, runner, tool versions) so downstream
# consumers can audit *what* was built and *how*.
#
# POSIX sh, no bashisms. Dependencies: sha256sum (or shasum -a 256), find,
# sort, python3 (only for JSON assembly; falls back to a printf template).
#
# Usage:
#   scripts/ci/checksums.sh [REPO_ROOT] [OUT_DIR]
# Defaults: REPO_ROOT=$GITHUB_WORKSPACE or ".", OUT_DIR="$REPO_ROOT/docs/generated/provenance"
set -eu

REPO_ROOT="${1:-${GITHUB_WORKSPACE:-.}}"
OUT_DIR="${2:-$REPO_ROOT/docs/generated/provenance}"

mkdir -p "$OUT_DIR"

# Pick a checksum tool.
if command -v sha256sum >/dev/null 2>&1; then
    SHA_CMD="sha256sum"
elif command -v shasum >/dev/null 2>&1; then
    SHA_CMD="shasum -a 256"
else
    echo "error: no sha256sum/shasum available" >&2
    exit 2
fi

SUMS_FILE="$OUT_DIR/SHA256SUMS"
: > "$SUMS_FILE"

# Collect artifacts: reactor JARs (exclude sources/javadoc to avoid noise? keep
# all for completeness) and the aggregate SBOMs.
# Use a temp list to keep deterministic ordering.
TMP_LIST=$(mktemp)
trap 'rm -f "$TMP_LIST"' EXIT

# Reactor JARs under */target and aggregate SBOM (bom.json/bom.xml) at root target.
find "$REPO_ROOT" -type f \
    \( -path '*/target/*.jar' -o -name 'bom.json' -o -name 'bom.xml' \) \
    ! -path '*/target/classes/*' ! -path '*/target/test-classes/*' \
    2>/dev/null | LC_ALL=C sort > "$TMP_LIST" || true

COUNT=0
while IFS= read -r f; do
    [ -f "$f" ] || continue
    rel=$(printf '%s' "$f" | sed "s|^$REPO_ROOT/||")
    line=$($SHA_CMD "$f" | awk '{print $1}')
    printf '%s  %s\n' "$line" "$rel" >> "$SUMS_FILE"
    COUNT=$((COUNT + 1))
done < "$TMP_LIST"

echo "checksums: hashed $COUNT artifact(s) -> $SUMS_FILE"

# Provenance metadata (best-effort; empty when not in CI).
GIT_COMMIT=$(git -C "$REPO_ROOT" rev-parse HEAD 2>/dev/null || echo "")
GIT_REF="${GITHUB_REF:-$(git -C "$REPO_ROOT" symbolic-ref -q HEAD 2>/dev/null || echo "")}"
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
JAVA_VER=$(java -version 2>&1 | head -1 | sed 's/"/\\"/g' || echo "")
MVN_VER=$(mvn -v 2>/dev/null | head -1 || echo "")
SUMS_HASH=$($SHA_CMD "$SUMS_FILE" | awk '{print $1}')

PROV_FILE="$OUT_DIR/provenance.json"

if command -v python3 >/dev/null 2>&1; then
    REPO_ROOT="$REPO_ROOT" GIT_COMMIT="$GIT_COMMIT" GIT_REF="$GIT_REF" \
    BUILD_TIME="$BUILD_TIME" JAVA_VER="$JAVA_VER" MVN_VER="$MVN_VER" \
    ARTIFACT_COUNT="$COUNT" SUMS_HASH="$SUMS_HASH" \
    GH_RUN_ID="${GITHUB_RUN_ID:-}" GH_WF="${GITHUB_WORKFLOW:-}" \
    GH_REPO="${GITHUB_REPOSITORY:-}" RUNNER_OS="${RUNNER_OS:-}" \
    python3 - "$PROV_FILE" <<'PY'
import json, os, sys
out = sys.argv[1]
doc = {
    "_type": "https://in-toto.io/Statement/v1",
    "predicateType": "https://slsa.dev/provenance/v1",
    "subject": {
        "sha256sums_file": "SHA256SUMS",
        "sha256sums_digest": os.environ.get("SUMS_HASH", ""),
        "artifact_count": int(os.environ.get("ARTIFACT_COUNT", "0") or 0),
    },
    "predicate": {
        "buildType": "https://github.com/demoiselle/framework/ci",
        "builder": {
            "repository": os.environ.get("GH_REPO", ""),
            "workflow": os.environ.get("GH_WF", ""),
            "run_id": os.environ.get("GH_RUN_ID", ""),
            "runner_os": os.environ.get("RUNNER_OS", ""),
        },
        "metadata": {
            "buildStartedOn": os.environ.get("BUILD_TIME", ""),
            "git_commit": os.environ.get("GIT_COMMIT", ""),
            "git_ref": os.environ.get("GIT_REF", ""),
            "java_version": os.environ.get("JAVA_VER", ""),
            "maven_version": os.environ.get("MVN_VER", ""),
        },
    },
}
with open(out, "w", encoding="utf-8") as fh:
    json.dump(doc, fh, indent=2, ensure_ascii=False)
    fh.write("\n")
PY
else
    # Minimal fallback without python3.
    {
        printf '{\n'
        printf '  "git_commit": "%s",\n' "$GIT_COMMIT"
        printf '  "git_ref": "%s",\n' "$GIT_REF"
        printf '  "build_time": "%s",\n' "$BUILD_TIME"
        printf '  "artifact_count": %s,\n' "$COUNT"
        printf '  "sha256sums_digest": "%s"\n' "$SUMS_HASH"
        printf '}\n'
    } > "$PROV_FILE"
fi

echo "provenance: wrote $PROV_FILE"
