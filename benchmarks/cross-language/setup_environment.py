#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Create the environment for Java versus Python/C moocore benchmarks."""

from __future__ import annotations

import platform
import re
import subprocess
import sys
import venv
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SCRIPT_DIR = Path(__file__).resolve().parent
ENVIRONMENT = SCRIPT_DIR / "venv"
REQUIREMENTS = SCRIPT_DIR / "requirements.txt"


def run(command: list[str], cwd: Path = ROOT) -> None:
    print("+", " ".join(command), flush=True)
    subprocess.run(command, cwd=cwd, check=True)


def environment_python() -> Path:
    if platform.system() == "Windows":
        return ENVIRONMENT / "Scripts" / "python.exe"
    return ENVIRONMENT / "bin" / "python"


def check_submodules() -> None:
    required = [
        ROOT / "moocore" / "python" / "pyproject.toml",
        ROOT / "testsuite" / "data",
    ]
    missing = [str(path.relative_to(ROOT)) for path in required if not path.exists()]
    if missing:
        raise SystemExit(
            "Required submodules are not initialized:\n  - "
            + "\n  - ".join(missing)
            + "\n\nRun: git submodule update --init moocore testsuite"
        )


def check_java() -> None:
    try:
        result = subprocess.run(
            ["java", "-version"], check=True, capture_output=True, text=True
        )
    except (OSError, subprocess.CalledProcessError) as error:
        raise SystemExit("Java 25 or newer is required and was not found") from error
    output = result.stdout + result.stderr
    match = re.search(r'version "(\d+)', output)
    if match is None or int(match.group(1)) < 25:
        raise SystemExit(
            "Java 25 or newer is required. Detected:\n" + output.strip()
        )


def main() -> None:
    if sys.version_info < (3, 10):
        raise SystemExit("Python 3.10 or newer is required")
    check_submodules()
    check_java()
    print(f"Creating benchmark environment at {ENVIRONMENT}")
    venv.EnvBuilder(with_pip=True).create(ENVIRONMENT)
    python = environment_python()
    run([
        str(python), "-m", "pip", "install", "-r", str(REQUIREMENTS)
    ])
    run([
        str(python), str(SCRIPT_DIR / "run.py"), "--check-dependencies",
    ])
    wrapper = "mvnw.cmd" if platform.system() == "Windows" else "./mvnw"
    run([wrapper, "-pl", "benchmarks", "-am", "-DskipTests", "clean", "package"])
    print("\nEnvironment ready. Run the full comparison with:")
    print(f"{python} {SCRIPT_DIR / 'run.py'}")


if __name__ == "__main__":
    main()
