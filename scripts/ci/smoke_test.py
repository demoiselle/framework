#!/usr/bin/env python3
"""Self-contained smoke tests for the CI helper scripts.

Runs each helper against synthetic fixtures and against the real repository,
asserting the expected outputs and exit codes. No third-party deps, no network.

Usage:
  python3 scripts/ci/smoke_test.py
Exit code 0 = all smoke tests passed.
"""
from __future__ import annotations

import json
import subprocess
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO_ROOT = HERE.parent.parent
PY = sys.executable


def run(args, expect_code=0):
    proc = subprocess.run(
        [PY, *args], capture_output=True, text=True, cwd=str(REPO_ROOT)
    )
    if proc.returncode != expect_code:
        raise AssertionError(
            f"cmd {args} exit {proc.returncode} (expected {expect_code})\n"
            f"STDOUT:\n{proc.stdout}\nSTDERR:\n{proc.stderr}"
        )
    return proc


results = []


def check(name, fn):
    try:
        fn()
        results.append((name, True, ""))
        print(f"PASS  {name}")
    except Exception as exc:  # noqa: BLE001
        results.append((name, False, str(exc)))
        print(f"FAIL  {name}: {exc}")


# ---- module_inventory ----
def t_inventory_json():
    p = run([str(HERE / "module_inventory.py"),
             "--repo-root", str(REPO_ROOT), "--format", "json"])
    data = json.loads(p.stdout)
    assert data["declared_module_count"] >= 1, "no modules discovered"
    assert data["totals"]["unit_tests"] >= 1, "no unit tests counted"
    assert any(m["module"] == "demoiselle-core" for m in data["modules"]), \
        "demoiselle-core missing"


def t_inventory_markdown():
    p = run([str(HERE / "module_inventory.py"),
             "--repo-root", str(REPO_ROOT), "--format", "markdown"])
    assert "Module & Test Inventory" in p.stdout


# ---- runtime_matrix ----
def t_runtime_matrix_md():
    p = run([str(HERE / "runtime_matrix.py"),
             "--repo-root", str(REPO_ROOT), "--format", "markdown"])
    assert "Runtime Support Matrix" in p.stdout
    assert "Consistency checks" in p.stdout


def t_runtime_matrix_check():
    # --check must pass against the real, consistent pom.xml
    run([str(HERE / "runtime_matrix.py"),
         "--repo-root", str(REPO_ROOT), "--format", "json", "--check"],
        expect_code=0)


# ---- openapi_breaking_change ----
def t_openapi_no_break():
    base = {
        "openapi": "3.0.0",
        "paths": {"/a": {"get": {"responses": {"200": {}}}}},
        "components": {"schemas": {"S": {"properties": {"x": {}}}}},
    }
    cand = {
        "openapi": "3.0.0",
        "paths": {
            "/a": {"get": {"responses": {"200": {}}}},
            "/b": {"get": {"responses": {"200": {}}}},  # additive
        },
        "components": {"schemas": {"S": {"properties": {"x": {}, "y": {}}}}},
    }
    with tempfile.TemporaryDirectory() as d:
        bp = Path(d) / "base.json"
        cp = Path(d) / "cand.json"
        bp.write_text(json.dumps(base))
        cp.write_text(json.dumps(cand))
        p = run([str(HERE / "openapi_breaking_change.py"), str(bp), str(cp)],
                expect_code=0)
        assert "BREAKING CHANGES: 0" in p.stdout


def t_openapi_break_removed_path():
    base = {"paths": {"/a": {"get": {"responses": {"200": {}}}}}}
    cand = {"paths": {}}
    with tempfile.TemporaryDirectory() as d:
        bp = Path(d) / "base.json"
        cp = Path(d) / "cand.json"
        bp.write_text(json.dumps(base))
        cp.write_text(json.dumps(cand))
        p = run([str(HERE / "openapi_breaking_change.py"), str(bp), str(cp)],
                expect_code=1)
        assert "path removed: /a" in p.stdout


def t_openapi_break_new_required_param():
    base = {"paths": {"/a": {"get": {"parameters": [], "responses": {"200": {}}}}}}
    cand = {"paths": {"/a": {"get": {"parameters": [
        {"name": "q", "in": "query", "required": True}], "responses": {"200": {}}}}}}
    with tempfile.TemporaryDirectory() as d:
        bp = Path(d) / "base.json"
        cp = Path(d) / "cand.json"
        bp.write_text(json.dumps(base))
        cp.write_text(json.dumps(cand))
        p = run([str(HERE / "openapi_breaking_change.py"), str(bp), str(cp)],
                expect_code=1)
        assert "new required parameter" in p.stdout


# ---- jar fingerprint compare ----
def t_fingerprint_compare_ok_and_mismatch():
    import zipfile
    with tempfile.TemporaryDirectory() as d:
        d = Path(d)
        # build a fake reactor: repo/mod/target/x.jar
        (d / "mod" / "target").mkdir(parents=True)
        jar = d / "mod" / "target" / "x.jar"
        with zipfile.ZipFile(jar, "w") as zf:
            zf.writestr("a.txt", "hello")
            zf.writestr("META-INF/MANIFEST.MF",
                        "Manifest-Version: 1.0\nBuild-Time: 2020\n")
        run1 = run([str(HERE / "_jar_fingerprint.py"), str(d)])
        # rewrite jar with different Build-Time only -> must still match
        with zipfile.ZipFile(jar, "w") as zf:
            zf.writestr("a.txt", "hello")
            zf.writestr("META-INF/MANIFEST.MF",
                        "Manifest-Version: 1.0\nBuild-Time: 2099\n")
        run2 = run([str(HERE / "_jar_fingerprint.py"), str(d)])
        f1 = d / "r1.json"
        f2 = d / "r2.json"
        f1.write_text(run1.stdout)
        f2.write_text(run2.stdout)
        run([str(HERE / "_jar_fingerprint.py"), "--compare", str(f1), str(f2)],
            expect_code=0)
        # now change real content -> must mismatch
        with zipfile.ZipFile(jar, "w") as zf:
            zf.writestr("a.txt", "changed")
        run3 = run([str(HERE / "_jar_fingerprint.py"), str(d)])
        f3 = d / "r3.json"
        f3.write_text(run3.stdout)
        run([str(HERE / "_jar_fingerprint.py"), "--compare", str(f1), str(f3)],
            expect_code=1)


check("module_inventory json", t_inventory_json)
check("module_inventory markdown", t_inventory_markdown)
check("runtime_matrix markdown", t_runtime_matrix_md)
check("runtime_matrix --check consistent", t_runtime_matrix_check)
check("openapi gate: no break", t_openapi_no_break)
check("openapi gate: removed path", t_openapi_break_removed_path)
check("openapi gate: new required param", t_openapi_break_new_required_param)
check("jar fingerprint compare ok+mismatch", t_fingerprint_compare_ok_and_mismatch)

failed = [r for r in results if not r[1]]
print()
print(f"{len(results) - len(failed)}/{len(results)} smoke tests passed")
sys.exit(1 if failed else 0)
