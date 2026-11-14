#!/usr/bin/env bash
# Rebuild FlexPRET core + emulator, then clean-rebuild and run SDK tests.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FP_ROOT="$(dirname "$SCRIPT_DIR")"

source "$FP_ROOT/env.bash"

echo "=== Cleaning core build ==="
rm -rf "$FP_ROOT/build"

echo "=== Configuring core ==="
cmake -B "$FP_ROOT/build" "$FP_ROOT"

echo "=== Building core + emulator ==="
cmake --build "$FP_ROOT/build"

echo "=== Installing artifacts to SDK ==="
cmake --install "$FP_ROOT/build"

echo "=== Cleaning SDK build ==="
rm -rf "$FP_SDK_PATH/build"

echo "=== Configuring SDK ==="
cmake -B "$FP_SDK_PATH/build" "$FP_SDK_PATH"

echo "=== Building SDK ==="
cmake --build "$FP_SDK_PATH/build"

echo "=== Running tests ==="
cd "$FP_SDK_PATH/build"
ctest --output-on-failure
