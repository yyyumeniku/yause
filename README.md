Yause — Development / run instructions
===================================

Quick notes for contributors:

- This project uses legacy Minecraft (1.12.2) dev tooling that requires a Java 17 toolchain for running Gradle tasks and the dev client reliably.
- If your system JVM is newer (for example Java 25), Gradle and Groovy may fail with "Unsupported class file major version" errors when the wrapper attempts to parse build plugins.

How to run the client locally

1) Use the included helper that sets up a Java 17 JVM for Gradle and the client:

```bash
./runclient-java17.sh
```

2) Alternatively set your JAVA_HOME to a Java 17 JDK and run Gradle normally:

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
./gradlew runclient
```

CI / GitHub Actions

- Configure your workflow to use Java 17 for Gradle and the client. Example (actions/setup-java):

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: 17
```

If you are intentionally using a newer JVM, you can bypass the local wrapper check by setting the environment variable ALLOW_MODERN_JDK=1, but this is not recommended for normal development.

If you want help updating CI or the repository to support newer JVMs, open an issue and I can help investigate compatibility changes.
