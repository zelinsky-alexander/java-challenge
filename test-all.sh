#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
for task in "$ROOT"/tasks/*; do
  mkdir -p "$task/out"
  javac -d "$task/out" "$task/src/Main.java" "$task/src/Test.java"
  java -ea -cp "$task/out" Test
  echo "PASS $(basename "$task")"
done
