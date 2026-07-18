#!/usr/bin/env python3
"""Collect deterministic EasyBangumi release-preflight evidence without modifying the repo."""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path


def run(root: Path, *args: str) -> str:
    result = subprocess.run(args, cwd=root, text=True, capture_output=True)
    return result.stdout.strip() if result.returncode == 0 else ""


def changed(root: Path, baseline: str, paths: list[str]) -> list[str]:
    if not baseline:
        return []
    return run(root, "git", "diff", "--name-only", baseline, "--", *paths).splitlines()


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    android = root / "buildSrc/src/main/java/com/heyanle/buildsrc/Android.kt"
    notes = root / "app/src/main/assets/update_log.txt"
    migrate = root / "app/src/main/java/com/heyanle/easybangumi4/Migrate.kt"
    source_root = root / "inner_source"
    asset_root = root / "app/src/main/assets/inner_source"
    text = android.read_text() if android.exists() else ""
    code = re.search(r"const\s+val\s+versionCode\s*=\s*(\d+)", text)
    name = re.search(r'const\s+val\s+versionName\s*=\s*"([^"]+)"', text)
    target = name.group(1) if name else "<missing>"
    tags = run(root, "git", "tag", "--merged", "HEAD", "--sort=-v:refname").splitlines()
    baseline = next((tag for tag in tags if tag != target), "<none>")
    note_head = next((line for line in notes.read_text().splitlines() if line.strip()), "<missing>") if notes.exists() else "<missing>"
    room_files = sorted((root / "app/src/main/java").rglob("*Database.kt"))
    room_summary = []
    for file in room_files:
        value = file.read_text()
        if "@Database" in value:
            match = re.search(r"version\s*=\s*(\d+)", value)
            room_summary.append(f"{file.relative_to(root)}: version {match.group(1) if match else '<missing>'}")
    print(f"target version: {target} (versionCode {code.group(1) if code else '<missing>'})")
    print(f"baseline tag: {baseline}")
    print(f"release-note first entry: {note_head}")
    print(f"root inner_source files: {len(list(source_root.glob('**/*')) if source_root.exists() else [])}")
    print(f"app asset inner_source exists: {asset_root.exists()}")
    print("application migration changes:")
    print("\n".join(changed(root, baseline if baseline != "<none>" else "", [str(migrate.relative_to(root))])) or "  none")
    print("Room/schema changes:")
    all_app_changes = changed(
        root,
        baseline if baseline != "<none>" else "",
        ["app/src/main/java/com/heyanle/easybangumi4", "app/schemas"],
    )
    room_changes = [
        path for path in all_app_changes
        if path == str(migrate.relative_to(root))
        or path.endswith("Database.kt")
        or "/entity/" in path
        or path.startswith("app/schemas/")
    ]
    print("\n".join(room_changes) or "  none")
    print("Room databases:")
    print("\n".join(f"  {line}" for line in room_summary) or "  none")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
