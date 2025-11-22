#!/usr/bin/env bash
set -euo pipefail

# Try to find Java 17 home on macOS using native helper
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  JAVA_17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
else
  JAVA_17_HOME=""
fi

# Try common locations (Temurin/Homebrew) if /usr/libexec/java_home doesn't find anything
if [ -z "${JAVA_17_HOME}" ]; then
  if [ -d "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home" ]; then
    JAVA_17_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
  elif [ -d "/Library/Java/JavaVirtualMachines/adoptopenjdk-17.jdk/Contents/Home" ]; then
    JAVA_17_HOME="/Library/Java/JavaVirtualMachines/adoptopenjdk-17.jdk/Contents/Home"
  fi
fi

if [ -z "${JAVA_17_HOME}" ]; then
  echo "Java 17 not found in common locations. Please install Java 17 (Temurin, OpenJDK 17) and retry."
  echo "If you have Java 17 installed, set JAVA_HOME manually to a JDK 17 location and re-run this script."
  exit 1
fi

echo "Using JAVA_HOME=${JAVA_17_HOME} to run the Gradle wrapper."
export JAVA_HOME="${JAVA_17_HOME}"
export PATH="${JAVA_HOME}/bin:$PATH"

# Explicitly call the wrapper with java toolchain settings if useful and run the client
./gradlew --no-daemon runClient "$@"
