#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Script for UNIX / Linux / macOS
# ----------------------------------------------------------------------------
set -e

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

echo "[ERROR] Maven command not found on PATH. Please ensure Maven or JDK is installed." >&2
exit 1
