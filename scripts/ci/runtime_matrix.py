#!/usr/bin/env python3
"""Generate a declared-and-verified runtime compatibility matrix.

The Demoiselle framework is runtime-agnostic (Jakarta EE 10 / CDI 4.0). This
script produces a matrix of the *declared* target runtimes together with the
*verifiable* facts we can assert from the repository itself, without inventing
compatibility claims.

Declared runtimes are sourced from ``docs/runtime-support.json`` if present;
otherwise a conservative built-in default derived from the project README is
used (Java 21 baseline, WildFly 27+, Quarkus, Open Liberty). Verified facts are
extracted from the root ``pom.xml``:

  - ``java.version`` / ``maven.compiler.release`` property
  - Maven Enforcer ``requireJavaVersion`` / ``requireMavenVersion`` ranges

The output is deterministic (sorted, no timestamps) so it can be regenerated in
CI and compared to the committed ``docs/generated/runtime-matrix.md`` to detect
drift. Standard library only; no network.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_DECLARED = {
    "baseline": {
        "java_min": "21",
        "jakarta_ee": "10",
        "cdi": "4.0",
        "maven_min": "3.9",
    },
    "runtimes": [
        {"name": "WildFly", "min_version": "27", "kind": "application-server",
         "status": "declared", "note": "Jakarta EE 10 full/web profile"},
        {"name": "Quarkus", "min_version": "3.x", "kind": "cloud-native",
         "status": "declared", "note": "CDI 4.0 / Arc; native image via Lite extensions"},
        {"name": "Open Liberty", "min_version": "23.x", "kind": "application-server",
         "status": "declared", "note": "Jakarta EE 10 feature set"},
    ],
}


def _strip_ns(tag: str) -> str:
    return tag.split("}", 1)[-1] if "}" in tag else tag


def _find_text(root, path_tags):
    """Depth-first search for a nested sequence of tag names, namespace agnostic."""
    def walk(elem, tags):
        if not tags:
            return elem.text.strip() if elem.text else ""
        head, rest = tags[0], tags[1:]
        for child in elem:
            if _strip_ns(child.tag) == head:
                res = walk(child, rest)
                if res:
                    return res
        return ""
    return walk(root, path_tags)


def extract_verified_facts(root_pom: Path) -> dict:
    facts = {
        "java_version_property": None,
        "compiler_release_property": None,
        "enforcer_require_java": None,
        "enforcer_require_maven": None,
    }
    try:
        tree = ET.parse(root_pom)
    except (ET.ParseError, OSError) as exc:
        raise SystemExit(f"error: cannot parse {root_pom}: {exc}")
    root = tree.getroot()

    # properties
    for elem in root.iter():
        if _strip_ns(elem.tag) == "properties":
            for prop in elem:
                name = _strip_ns(prop.tag)
                if name == "java.version":
                    facts["java_version_property"] = (prop.text or "").strip()
                elif name == "maven.compiler.release":
                    facts["compiler_release_property"] = (prop.text or "").strip()

    # enforcer rules (search raw text for the version ranges to stay robust)
    raw = root_pom.read_text(encoding="utf-8", errors="replace")
    m = re.search(r"<requireJavaVersion>\s*<version>([^<]+)</version>", raw)
    if m:
        facts["enforcer_require_java"] = m.group(1).strip()
    m = re.search(r"<requireMavenVersion>\s*<version>([^<]+)</version>", raw)
    if m:
        facts["enforcer_require_maven"] = m.group(1).strip()
    return facts


def load_declared(repo_root: Path) -> dict:
    cfg = repo_root / "docs" / "runtime-support.json"
    if cfg.is_file():
        try:
            return json.loads(cfg.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError) as exc:
            print(f"warning: ignoring invalid {cfg}: {exc}", file=sys.stderr)
    return DEFAULT_DECLARED


def build_matrix(repo_root: Path) -> dict:
    root_pom = repo_root / "pom.xml"
    if not root_pom.is_file():
        raise SystemExit(f"error: root pom.xml not found at {root_pom}")
    declared = load_declared(repo_root)
    verified = extract_verified_facts(root_pom)

    # Consistency verdicts derived purely from repo facts.
    checks = []
    baseline = declared.get("baseline", {})
    java_min = baseline.get("java_min")
    jp = verified.get("java_version_property")
    checks.append({
        "check": "java.version property matches declared baseline",
        "expected": java_min,
        "actual": jp,
        "ok": bool(java_min) and jp == java_min,
    })
    ej = verified.get("enforcer_require_java") or ""
    checks.append({
        "check": "enforcer requireJavaVersion lower bound matches baseline",
        "expected": f"[{java_min},)",
        "actual": ej,
        "ok": bool(java_min) and ej.replace(" ", "") == f"[{java_min},)",
    })
    mm = baseline.get("maven_min")
    em = verified.get("enforcer_require_maven") or ""
    checks.append({
        "check": "enforcer requireMavenVersion lower bound matches baseline",
        "expected": f"[{mm},)",
        "actual": em,
        "ok": bool(mm) and em.replace(" ", "") == f"[{mm},)",
    })

    return {
        "baseline": baseline,
        "runtimes": sorted(declared.get("runtimes", []), key=lambda r: r["name"]),
        "verified_facts": verified,
        "consistency_checks": checks,
        "all_checks_ok": all(c["ok"] for c in checks),
    }


def render_markdown(m: dict) -> str:
    b = m["baseline"]
    lines = ["# Runtime Support Matrix", ""]
    lines.append("> Generated by `scripts/ci/runtime_matrix.py`. Do not edit by hand.")
    lines.append("")
    lines.append("## Baseline (declared)")
    lines.append("")
    lines.append("| Property | Value |")
    lines.append("|----------|-------|")
    for k in ("java_min", "jakarta_ee", "cdi", "maven_min"):
        if k in b:
            lines.append(f"| {k} | {b[k]} |")
    lines.append("")
    lines.append("## Target runtimes (declared)")
    lines.append("")
    lines.append("| Runtime | Min version | Kind | Status | Note |")
    lines.append("|---------|-------------|------|--------|------|")
    for r in m["runtimes"]:
        lines.append(
            f"| {r.get('name','-')} | {r.get('min_version','-')} "
            f"| {r.get('kind','-')} | {r.get('status','-')} | {r.get('note','')} |"
        )
    lines.append("")
    lines.append("## Verified facts (from pom.xml)")
    lines.append("")
    lines.append("| Fact | Value |")
    lines.append("|------|-------|")
    for k, v in m["verified_facts"].items():
        lines.append(f"| {k} | {v if v is not None else '-'} |")
    lines.append("")
    lines.append("## Consistency checks")
    lines.append("")
    lines.append("| Check | Expected | Actual | Result |")
    lines.append("|-------|----------|--------|--------|")
    for c in m["consistency_checks"]:
        res = "✅" if c["ok"] else "❌"
        lines.append(
            f"| {c['check']} | `{c['expected']}` | `{c['actual']}` | {res} |"
        )
    lines.append("")
    lines.append(f"**All checks passed:** {'yes' if m['all_checks_ok'] else 'no'}")
    lines.append("")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo-root",
        default=os.environ.get("GITHUB_WORKSPACE", "."),
    )
    parser.add_argument("--format", choices=["json", "markdown"], default="markdown")
    parser.add_argument("--out")
    parser.add_argument(
        "--check", action="store_true",
        help="Exit non-zero if consistency checks fail",
    )
    args = parser.parse_args(argv)

    repo_root = Path(args.repo_root).resolve()
    matrix = build_matrix(repo_root)

    rendered = (
        json.dumps(matrix, indent=2, ensure_ascii=False) + "\n"
        if args.format == "json"
        else render_markdown(matrix)
    )
    if args.out:
        Path(args.out).parent.mkdir(parents=True, exist_ok=True)
        Path(args.out).write_text(rendered, encoding="utf-8")
    else:
        sys.stdout.write(rendered)

    if args.check and not matrix["all_checks_ok"]:
        print("runtime matrix: consistency checks FAILED", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
