#!/usr/bin/env python3
"""OpenAPI breaking-change gate.

Compares two OpenAPI documents (baseline vs. candidate, JSON or YAML) and fails
when a *breaking* change is detected. This is a pragmatic, dependency-light
checker intended as a CI gate for the ``demoiselle-openapi`` module and for any
service that publishes an OpenAPI contract.

A change is considered BREAKING when the candidate does any of:

  * removes an existing path
  * removes an operation (HTTP method) from an existing path
  * removes a previously documented response status code from an operation
  * adds a NEW *required* request parameter to an existing operation
  * makes a previously optional parameter required
  * removes an existing property from a component schema
  * makes a previously optional schema property required

Non-breaking additions (new paths, new operations, new optional parameters, new
schemas/properties) are reported as informational only.

YAML support is optional: if PyYAML is unavailable, only JSON inputs are
accepted (the framework's generated specs are JSON). Standard library first.

Exit codes:
  0  no breaking changes
  1  breaking changes detected
  2  usage / parse error
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

try:  # optional
    import yaml  # type: ignore
    _HAVE_YAML = True
except Exception:  # pragma: no cover
    _HAVE_YAML = False


def load_spec(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    suffix = path.suffix.lower()
    if suffix in (".yaml", ".yml"):
        if not _HAVE_YAML:
            raise SystemExit(
                f"error: {path} is YAML but PyYAML is not installed"
            )
        data = yaml.safe_load(text)
    else:
        try:
            data = json.loads(text)
        except json.JSONDecodeError:
            if _HAVE_YAML:
                data = yaml.safe_load(text)
            else:
                raise SystemExit(f"error: cannot parse {path} as JSON")
    if not isinstance(data, dict):
        raise SystemExit(f"error: {path} is not an OpenAPI object")
    return data


_HTTP_METHODS = {"get", "put", "post", "delete", "options", "head", "patch", "trace"}


def _operations(path_item: dict) -> dict:
    return {k: v for k, v in path_item.items() if k.lower() in _HTTP_METHODS}


def _required_params(operation: dict) -> set[str]:
    req = set()
    for p in operation.get("parameters", []) or []:
        if isinstance(p, dict) and p.get("required") is True and "name" in p:
            req.add(f"{p.get('in','?')}:{p['name']}")
    return req


def _all_params(operation: dict) -> set[str]:
    out = set()
    for p in operation.get("parameters", []) or []:
        if isinstance(p, dict) and "name" in p:
            out.add(f"{p.get('in','?')}:{p['name']}")
    return out


def _schemas(spec: dict) -> dict:
    return (spec.get("components", {}) or {}).get("schemas", {}) or {}


def diff(baseline: dict, candidate: dict) -> tuple[list[str], list[str]]:
    breaking: list[str] = []
    info: list[str] = []

    b_paths = baseline.get("paths", {}) or {}
    c_paths = candidate.get("paths", {}) or {}

    for path, b_item in b_paths.items():
        if path not in c_paths:
            breaking.append(f"path removed: {path}")
            continue
        c_item = c_paths[path]
        b_ops = _operations(b_item)
        c_ops = _operations(c_item)
        for method, b_op in b_ops.items():
            if method not in c_ops:
                breaking.append(f"operation removed: {method.upper()} {path}")
                continue
            c_op = c_ops[method]

            # responses removed
            b_resp = set((b_op.get("responses", {}) or {}).keys())
            c_resp = set((c_op.get("responses", {}) or {}).keys())
            for code in sorted(b_resp - c_resp):
                breaking.append(
                    f"response removed: {method.upper()} {path} -> {code}"
                )

            # required params
            b_req = _required_params(b_op)
            c_req = _required_params(c_op)
            b_all = _all_params(b_op)
            for p in sorted(c_req - b_req):
                if p not in b_all:
                    breaking.append(
                        f"new required parameter: {method.upper()} {path} [{p}]"
                    )
                else:
                    breaking.append(
                        f"parameter became required: {method.upper()} {path} [{p}]"
                    )

    for path in sorted(set(c_paths) - set(b_paths)):
        info.append(f"path added: {path}")

    # schemas
    b_schemas = _schemas(baseline)
    c_schemas = _schemas(candidate)
    for name, b_schema in b_schemas.items():
        if name not in c_schemas:
            breaking.append(f"schema removed: {name}")
            continue
        c_schema = c_schemas[name]
        b_props = set((b_schema.get("properties", {}) or {}).keys())
        c_props = set((c_schema.get("properties", {}) or {}).keys())
        for prop in sorted(b_props - c_props):
            breaking.append(f"schema property removed: {name}.{prop}")
        b_required = set(b_schema.get("required", []) or [])
        c_required = set(c_schema.get("required", []) or [])
        for prop in sorted(c_required - b_required):
            breaking.append(f"schema property became required: {name}.{prop}")
        for prop in sorted(c_props - b_props):
            info.append(f"schema property added: {name}.{prop}")

    for name in sorted(set(c_schemas) - set(b_schemas)):
        info.append(f"schema added: {name}")

    return breaking, info


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", help="Baseline OpenAPI spec (JSON/YAML)")
    parser.add_argument("candidate", help="Candidate OpenAPI spec (JSON/YAML)")
    parser.add_argument("--format", choices=["text", "json"], default="text")
    parser.add_argument("--out")
    args = parser.parse_args(argv)

    b_path, c_path = Path(args.baseline), Path(args.candidate)
    for p in (b_path, c_path):
        if not p.is_file():
            raise SystemExit(f"error: file not found: {p}")

    breaking, info = diff(load_spec(b_path), load_spec(c_path))

    if args.format == "json":
        payload = {
            "breaking": breaking,
            "informational": info,
            "breaking_count": len(breaking),
        }
        rendered = json.dumps(payload, indent=2, ensure_ascii=False) + "\n"
    else:
        parts = []
        parts.append("# OpenAPI breaking-change gate")
        parts.append("")
        if breaking:
            parts.append(f"BREAKING CHANGES: {len(breaking)}")
            parts.extend(f"  - {x}" for x in breaking)
        else:
            parts.append("BREAKING CHANGES: 0")
        if info:
            parts.append("")
            parts.append(f"Non-breaking changes: {len(info)}")
            parts.extend(f"  + {x}" for x in info)
        rendered = "\n".join(parts) + "\n"

    if args.out:
        Path(args.out).write_text(rendered, encoding="utf-8")
    sys.stdout.write(rendered)

    return 1 if breaking else 0


if __name__ == "__main__":
    raise SystemExit(main())
