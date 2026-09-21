# Contributing to Angular Files Navigator

Thank you for helping improve Angular Files Navigator. Contributions are
welcome from everyone, whether you are fixing a typo, reporting a bug, or
adding a feature.

## Before you start

- Read the [Code of Conduct](CODE_OF_CONDUCT.md).
- Search open and closed issues and pull requests.
- For a substantial change, open an issue first so the approach can be
  discussed before implementation.
- Do not include credentials, private project files, or other sensitive data in
  issues, commits, or pull requests.

## Development setup

1. Install JDK 17 or newer.
2. Fork and clone the repository.
3. Create a topic branch:

   ```bash
   git checkout -b fix/short-description
   ```

4. Build the plugin:

   ```bash
   ./gradlew buildPlugin
   ```

   On Windows, use `gradlew.bat buildPlugin`.

5. Launch a sandbox IDE while developing:

   ```bash
   ./gradlew runIde
   ```

## Making changes

- Keep changes focused and consistent with the existing Kotlin, TypeScript, and
  Gradle conventions. IntelliJ-specific code belongs in `intellij/`; VS Code
  code belongs in `vscode/`.
- Preserve compatibility with IntelliJ Platform build `233` and newer unless
  the change intentionally updates that support policy.
- Prefer clear names and small, reusable functions.
- Update the README or other documentation when user-visible behavior changes.
- Add or update tests when a testable behavior changes.
- Do not commit generated output from `build/`, `.gradle/`, or IDE settings.

## Pull requests

Before opening a pull request:

```bash
./gradlew buildPlugin
```

In the pull request description, explain:

- What changed and why.
- How the change was tested.
- Any compatibility, migration, or documentation considerations.
- Screenshots or short recordings for visible UI changes, when useful.

Keep each pull request focused. Maintainers may ask for changes to improve
scope, test coverage, documentation, or compatibility.

## Reporting bugs and requesting features

Use the GitHub issue templates where possible. A useful bug report includes
the IDE name and version, plugin version, operating system, example file
names, expected behavior, actual behavior, and reproduction steps.

For security issues, do not open a public issue. Follow
[`SECURITY.md`](SECURITY.md).
