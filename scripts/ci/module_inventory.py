#!/usr/bin/env python3
"""Dynamic module/test inventory for the Demoiselle multi-module build.

Reads the reactor from the root ``pom.xml`` (the ``<modules>`` list), then for
each module directory counts unit tests (``*Test.java``) and integration/contract
tests (``*IT.java``) under ``src/test``. It also cross-checks module directories
that contain a ``pom.xml`` but are NOT declared in the reactor, surfacing them as
``undeclared`` so drift is visible in CI without editing any POM.

Output formats:
  - ``--format json``     machine-readable inventory (default)
  - ``--format markdown`` GitHub step-summary friendly tables

The script is self-contained: standard library only, no third-party deps, no
network. XML parsing uses ``xml.etree`` and tolerates namespaced POMs.

Exit codes:
  0  success
  1  inventory consistency check failed (--check)
  2  root pom.xml not found / unreadable
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def _strip_ns(tag: str) -> str:
    return tag.split("}", 1)[-1] if "}" in tag else tag


def read_reactor_modules(root_pom: Path) -> list[str]:
    """Return declared module directory names from a root pom.xml."""
    try:
        tree = ET.parse(root_pom)
    except (ET.ParseError, OSError) as exc:  # pragma: no cover - defensive
        raise SystemExit(f"error: cannot parse {root_pom}: {exc}")
    root = tree.getroot()
    modules: list[str] = []
    for elem in root.iter():
        if _strip_ns(elem.tag) == "module" and elem.text:
            modules.append(elem.text.strip())
    return modules


def count_tests(module_dir: Path) -> dict[str, int]:
    """Count unit and integration test source files under src/test."""
    test_root = module_dir / "src" / "test"
    unit = integration = 0
    if test_root.is_dir():
        for path in test_root.rglob("*.java"):
            name = path.name
            if name.endswith("IT.java"):
                integration += 1
            elif name.endswith("Test.java"):
                unit += 1
    return {"unit_tests": unit, "integration_tests": integration}


def module_artifact_id(module_dir: Path) -> str | None:
    pom = module_dir / "pom.xml"
    if not pom.is_file():
        return None
    try:
        tree = ET.parse(pom)
    except (ET.ParseError, OSError):
        return None
    root = tree.getroot()
    # artifactId at project level is the first non-parent artifactId child
    for child in root:
        if _strip_ns(child.tag) == "artifactId" and child.text:
            return child.text.strip()
    return None


def build_inventory(repo_root: Path) -> dict:
    root_pom = repo_root / "pom.xml"
    if not root_pom.is_file():
        raise SystemExit(f"error: root pom.xml not found at {root_pom}")

    declared = read_reactor_modules(root_pom)
    declared_set = set(declared)

    modules = []
    total_unit = total_it = 0
    for name in declared:
        mod_dir = repo_root / name
        counts = count_tests(mod_dir)
        total_unit += counts["unit_tests"]
        total_it += counts["integration_tests"]
        modules.append(
            {
                "module": name,
                "artifact_id": module_artifact_id(mod_dir),
                "exists": mod_dir.is_dir(),
                "has_pom": (mod_dir / "pom.xml").is_file(),
                **counts,
            }
        )

    # Detect directories with a pom.xml that are not declared in the reactor.
    undeclared = []
    for entry in sorted(repo_root.iterdir()):
        if not entry.is_dir():
            continue
        if entry.name in declared_set:
            continue
        if (entry / "pom.xml").is_file() and entry.name.startswith("demoiselle-"):
            undeclared.append(entry.name)

    return {
        "repo_root": str(repo_root),
        "declared_module_count": len(declared),
        "modules": modules,
        "undeclared_modules_with_pom": undeclared,
        "totals": {
            "unit_tests": total_unit,
            "integration_tests": total_it,
            "all_tests": total_unit + total_it,
        },
    }


def render_markdown(inv: dict) -> str:
    lines = []
    lines.append("## 🧩 Module & Test Inventory\n")
    t = inv["totals"]
    lines.append(
        f"**Declared modules:** {inv['declared_module_count']} · "
        f"**Unit tests:** {t['unit_tests']} · "
        f"**Integration/contract tests:** {t['integration_tests']} · "
        f"**Total:** {t['all_tests']}\n"
    )
    lines.append("| Module | artifactId | Unit | IT | pom |")
    lines.append("|--------|-----------|------|----|-----|")
    for m in inv["modules"]:
        pom = "✅" if m["has_pom"] else "❌"
        lines.append(
            f"| {m['module']} | {m['artifact_id'] or '-'} "
            f"| {m['unit_tests']} | {m['integration_tests']} | {pom} |"
        )
    if inv["undeclared_modules_with_pom"]:
        lines.append("")
        lines.append("> ⚠️ **Undeclared modules** (have `pom.xml` but not in reactor): "
                     + ", ".join(inv["undeclared_modules_with_pom"]))
    return "\n".join(lines) + "\n"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo-root",
        default=os.environ.get("GITHUB_WORKSPACE", "."),
        help="Repository root (default: $GITHUB_WORKSPACE or current dir)",
    )
    parser.add_argument(
        "--format", choices=["json", "markdown"], default="json"
    )
    parser.add_argument("--out", help="Write output to file instead of stdout")
    parser.add_argument(
        "--check", action="store_true",
        help="Fail when a declared module is missing/pom-less or a module POM is outside the reactor",
    )
    args = parser.parse_args(argv)

    repo_root = Path(args.repo_root).resolve()
    inv = build_inventory(repo_root)

    if args.format == "json":
        rendered = json.dumps(inv, indent=2, ensure_ascii=False) + "\n"
    else:
        rendered = render_markdown(inv)

    if args.out:
        Path(args.out).write_text(rendered, encoding="utf-8")
    else:
        sys.stdout.write(rendered)

    inconsistent = bool(inv["undeclared_modules_with_pom"]) or any(
        not module["exists"] or not module["has_pom"] for module in inv["modules"]
    )
    if args.check and inconsistent:
        print("module inventory: consistency check FAILED", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
