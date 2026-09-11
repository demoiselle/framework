#!/usr/bin/env python3
"""Validate the self-contained Demoiselle GitHub Pages source in ``/docs``.

Checks Jekyll page front matter, layouts/includes, public permalinks, local routes,
portal HTML IDs and baseline accessibility hooks. This is a source-level gate;
the GitHub Pages Jekyll build remains the rendering authority.
"""
from __future__ import annotations

import json
import re
import sys
from html.parser import HTMLParser
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PORTAL_ROOT = REPO_ROOT / "docs"

REQUIRED_FILES = (
    "_config.yml",
    "index.md",
    "guide.md",
    "404.md",
    "_layouts/portal.html",
    "_layouts/default.html",
    "_includes/head.html",
    "_includes/site-header.html",
    "_includes/footer.html",
    "_includes/brand-mark.html",
    "_includes/icon.html",
    "assets/css/portal.css",
    "assets/js/portal.js",
    "assets/img/favicon.svg",
    "assets/img/favicon-32.png",
    "assets/img/apple-touch-icon.png",
    "assets/img/icon-192.png",
    "assets/img/icon-512.png",
    "assets/img/icon-maskable-512.png",
    "assets/img/og-cover.png",
    "assets/img/demoiselle-symbol.svg",
    "assets/img/demoiselle-logo.svg",
    "assets/img/demoiselle-logo-inverse.svg",
    "site.webmanifest",
    "robots.txt",
    "sitemap.xml",
    "migration-4.1.md",
    "production-extensions.md",
    "roadmap.md",
)

PAGES = {
    "index.md": ("portal", "/"),
    "404.md": ("portal", "/404.html"),
    "guide.md": ("default", "/docs/"),
    "migration-4.1.md": ("default", "/docs/migration-4.1.html"),
    "production-extensions.md": ("default", "/docs/production-extensions.html"),
    "roadmap.md": ("default", "/docs/roadmap.html"),
}

PUBLIC_ROUTES = {
    permalink: PORTAL_ROOT / relative
    for relative, (_, permalink) in PAGES.items()
}
PUBLIC_ROUTES.update({
    "/robots.txt": PORTAL_ROOT / "robots.txt",
    "/sitemap.xml": PORTAL_ROOT / "sitemap.xml",
})


def fail(message: str) -> None:
    raise AssertionError(message)


def display(path: Path) -> str:
    return str(path.relative_to(REPO_ROOT))


def front_matter(path: Path) -> dict[str, str]:
    text = path.read_text(encoding="utf-8")
    if not text.startswith("---\n") and not text.startswith("---\r\n"):
        fail(f"missing YAML front matter: {display(path)}")
    normalized = text.replace("\r\n", "\n")
    end = normalized.find("\n---\n", 4)
    if end < 0:
        fail(f"unterminated YAML front matter: {display(path)}")
    values: dict[str, str] = {}
    for line in normalized[4:end].splitlines():
        if ":" not in line:
            continue
        key, value = line.split(":", 1)
        values[key.strip()] = value.strip().strip('"\'')
    return values


def route_target(route: str) -> Path | None:
    path = route.split("#", 1)[0].split("?", 1)[0] or "/"
    if path in PUBLIC_ROUTES:
        return PUBLIC_ROUTES[path]
    relative = path.lstrip("/")
    candidate = PORTAL_ROOT / relative
    return candidate if candidate.exists() else None


class PortalParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.ids: list[str] = []
        self.fragment_links: list[str] = []
        self.h1_count = 0

    def handle_starttag(self, tag: str, attrs) -> None:
        data = dict(attrs)
        if data.get("id"):
            self.ids.append(data["id"])
        href = data.get("href", "")
        if href.startswith("#") and len(href) > 1:
            self.fragment_links.append(href[1:])
        if tag == "h1":
            self.h1_count += 1


def main() -> int:
    for relative in REQUIRED_FILES:
        if not (PORTAL_ROOT / relative).is_file():
            fail(f"required Pages file missing: docs/{relative}")

    config = (PORTAL_ROOT / "_config.yml").read_text(encoding="utf-8")
    if not re.search(r'^url:\s*["\']https://demoiselle\.github\.io["\']\s*$', config, re.M):
        fail("docs/_config.yml must declare the public GitHub Pages url")
    if not re.search(r'^baseurl:\s*["\']/framework["\']\s*$', config, re.M):
        fail("docs/_config.yml baseurl must be /framework for project Pages")

    workflow_path = REPO_ROOT / ".github/workflows/static.yml"
    if not workflow_path.is_file():
        fail("Pages deployment workflow missing: .github/workflows/static.yml")
    workflow = workflow_path.read_text(encoding="utf-8")
    for required in ("source: ./docs", "destination: ./_site", "path: ./_site"):
        if required not in workflow:
            fail(f"Pages workflow must declare {required!r}")
    if re.search(r"uses:\s+[^\s@]+@v\d+", workflow):
        fail("Pages workflow actions must be pinned to full commit SHAs")
    action_refs = re.findall(r"uses:\s+[^\s@]+@([0-9a-f]{40})(?:\s|$)", workflow)
    if len(action_refs) != 5:
        fail(f"Pages workflow must contain 5 SHA-pinned action references, found {len(action_refs)}")

    for relative, (expected_layout, expected_permalink) in PAGES.items():
        path = PORTAL_ROOT / relative
        values = front_matter(path)
        if values.get("layout") != expected_layout:
            fail(f"{display(path)}: expected layout {expected_layout!r}")
        if values.get("permalink") != expected_permalink:
            fail(f"{display(path)}: expected permalink {expected_permalink!r}")
        layout = PORTAL_ROOT / "_layouts" / f"{expected_layout}.html"
        if not layout.exists():
            fail(f"{display(path)}: missing layout {display(layout)}")

    source_files = [
        *(PORTAL_ROOT / "_layouts").glob("*.html"),
        *(PORTAL_ROOT / "_includes").glob("*.html"),
        PORTAL_ROOT / "index.md",
        PORTAL_ROOT / "404.md",
    ]
    include_count = 0
    route_count = 0
    for source in source_files:
        text = source.read_text(encoding="utf-8")
        for include in re.findall(r"{%\s*include\s+([^\s%]+)\s*%}", text):
            include_count += 1
            if not (PORTAL_ROOT / "_includes" / include).is_file():
                fail(f"{display(source)}: missing include {include}")
        for route in re.findall(r"{{\s*'([^']+)'\s*\|\s*relative_url\s*}}", text):
            route_count += 1
            if route_target(route) is None:
                fail(f"{display(source)}: unresolved local route {route}")

    # Brand assets and icon set wiring
    head = (PORTAL_ROOT / "_includes/head.html").read_text(encoding="utf-8")
    for needle in (
        "rel=\"icon\"",
        "rel=\"apple-touch-icon\"",
        "rel=\"manifest\"",
        "og:image",
        "twitter:card",
    ):
        if needle not in head:
            fail(f"docs/_includes/head.html must declare {needle}")

    manifest_path = PORTAL_ROOT / "site.webmanifest"
    front_matter(manifest_path)  # Jekyll only renders Liquid in files with front matter
    manifest_body = re.sub(
        r"^---\s*\n.*?\n---\s*\n", "", manifest_path.read_text(encoding="utf-8"),
        count=1, flags=re.S,
    )
    rendered = re.sub(r"{{[^}]*\|\s*jsonify\s*}}", '"x"', manifest_body)
    rendered = re.sub(r"{{[^}]*}}", "/x", rendered)
    try:
        manifest = json.loads(rendered)
    except json.JSONDecodeError as exc:
        fail(f"docs/site.webmanifest is not valid JSON after Liquid rendering: {exc}")
    if not manifest.get("icons"):
        fail("docs/site.webmanifest must declare icons")
    if not any(icon.get("purpose") == "maskable" for icon in manifest["icons"]):
        fail("docs/site.webmanifest must declare a maskable icon")

    icon_source = (PORTAL_ROOT / "_includes/icon.html").read_text(encoding="utf-8")
    available_icons = set(re.findall(r"{%-?\s*when\s+'([^']+)'\s*-?%}", icon_source))
    if not available_icons:
        fail("docs/_includes/icon.html declares no icons")
    used_icons = set()
    for source in source_files:
        text = source.read_text(encoding="utf-8")
        used_icons.update(re.findall(r'{%\s*include\s+icon\.html\s+name="([^"]+)"', text))
    unknown_icons = sorted(used_icons - available_icons)
    if unknown_icons:
        fail(f"portal references undefined icons: {', '.join(unknown_icons)}")

    portal = (PORTAL_ROOT / "index.md").read_text(encoding="utf-8")
    portal = re.sub(r"^---\s*\n.*?\n---\s*\n", "", portal, count=1, flags=re.S)
    parser = PortalParser()
    parser.feed(portal)
    duplicates = sorted({value for value in parser.ids if parser.ids.count(value) > 1})
    if duplicates:
        fail(f"duplicate portal IDs: {', '.join(duplicates)}")
    missing_fragments = sorted(set(parser.fragment_links) - set(parser.ids))
    if missing_fragments:
        fail(f"portal fragment targets missing: {', '.join(missing_fragments)}")
    if parser.h1_count != 1:
        fail(f"portal must contain exactly one h1, found {parser.h1_count}")

    portal_layout = (PORTAL_ROOT / "_layouts/portal.html").read_text(encoding="utf-8")
    docs_layout = (PORTAL_ROOT / "_layouts/default.html").read_text(encoding="utf-8")
    css = (PORTAL_ROOT / "assets/css/portal.css").read_text(encoding="utf-8")
    header = (PORTAL_ROOT / "_includes/site-header.html").read_text(encoding="utf-8")
    if 'class="skip-link"' not in portal_layout or 'class="skip-link"' not in docs_layout:
        fail("both layouts must provide a skip link")
    if 'aria-label="Navegação principal"' not in header:
        fail("main navigation needs an accessible label")
    if "prefers-reduced-motion" not in css or ":focus-visible" not in css:
        fail("CSS must preserve reduced-motion and visible-focus accessibility")
    templates = "\n".join(path.read_text(encoding="utf-8") for path in source_files)
    if re.search(r'<(?:script|link)[^>]+(?:src|href)=["\']https?://', templates):
        fail("portal must not load third-party scripts or styles")

    print(
        "PORTAL_CHECK: OK "
        f"(source=docs, {len(PAGES)} pages, {include_count} includes, "
        f"{route_count} local routes, {len(parser.ids)} portal IDs, "
        f"{len(used_icons)}/{len(available_icons)} icons used)"
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (AssertionError, OSError, UnicodeError) as exc:
        print(f"PORTAL_CHECK: FAIL — {exc}", file=sys.stderr)
        sys.exit(1)
