#!/usr/bin/env bash
# Convenience helper: run the Gradle runClient task using Java 17 on macOS
# - Uses /usr/libexec/java_home -v 17 if present
# - Falls back to the current JAVA_HOME if that fails
set -euo pipefail
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  JAVA17=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
  if [ -n "$JAVA17" ]; then
    echo "Using Java 17 at: $JAVA17"
    export JAVA_HOME="$JAVA17"
  else
    echo "Java 17 not found via /usr/libexec/java_home; using current JAVA_HOME (if any)."
  fi
else
  echo "/usr/libexec/java_home not found — ensure JAVA_HOME points to Java 17 before running this script."
fi
# Use Gradle wrapper so the environment is consistent
./gradlew runClient
