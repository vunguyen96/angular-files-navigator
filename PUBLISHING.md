# Publishing Angular Files Navigator to the JetBrains Marketplace

## 1. One-time preparation

1. **Pick a unique plugin ID.** The ID in `intellij/src/main/resources/META-INF/plugin.xml`
   (`com.vna.angularfilesnavigator`) must be globally unique on the Marketplace.
   Keep it stable once the plugin is published. The current project ID is
   `com.vna.angularfilesnavigator`.
2. **Fill in the vendor block** in `plugin.xml` (`<vendor>` name and URL;
   add an email if Marketplace publishing requires one).
3. **Create a JetBrains account** and sign in at <https://plugins.jetbrains.com>.
4. **Create a Marketplace vendor profile** (Marketplace requires you to be a vendor
   before your first upload).

## 2. First upload (manual)

The first version of any plugin must be uploaded manually so the Marketplace team can
review it:

1. Build the artifact:
   ```bash
   ./gradlew buildPlugin
   ```
2. Go to <https://plugins.jetbrains.com/plugin/add>.
3. Upload the ZIP generated in `intellij/build/distributions/`.
4. Choose a category (e.g. **Languages / Frameworks**) and a license, then submit.
5. Wait for approval (usually a couple of business days).

## 3. Signing (recommended, required for automated publishing)

JetBrains recommends signing plugins. Generate a certificate chain and private key:

```bash
# private key
openssl genpkey -aes-256-cbc -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:4096
# certificate (self-signed is accepted by the Marketplace)
openssl req -key private.pem -new -x509 -days 3650 -out chain.crt
```

Expose them to Gradle via environment variables (already wired in
`intellij/build.gradle.kts`):

| Variable               | Value                              |
|------------------------|------------------------------------|
| `CERTIFICATE_CHAIN`    | contents of `chain.crt`            |
| `PRIVATE_KEY`          | contents of `private.pem`          |
| `PRIVATE_KEY_PASSWORD` | the password you chose above       |

Then:

```bash
./gradlew signPlugin verifyPluginSignature
```

## 4. Automated publishing (subsequent versions)

1. Create a **Marketplace permanent token**:
   Marketplace → your profile → **My Tokens** → generate a token.
2. Export it and publish:
   ```bash
   export PUBLISH_TOKEN=perm:xxxxxxxx
   export CERTIFICATE_CHAIN="$(cat chain.crt)"
   export PRIVATE_KEY="$(cat private.pem)"
   export PRIVATE_KEY_PASSWORD=your-password
   ./gradlew publishPlugin
   ```
   `publishPlugin` bumps the version listing automatically. Increment `version` in
   `intellij/build.gradle.kts` for each release and update `<change-notes>` in
   `intellij/src/main/resources/META-INF/plugin.xml`.

## 5. Compatibility range

`patchPluginXml` in `intellij/build.gradle.kts` sets `since-build = 233` (2023.3)
and leaves the upper bound open,
so the plugin stays compatible with current and future IDE builds. Set an explicit
`untilBuild` in `build.gradle.kts` if you ever need to cap it.
Run the official compatibility check before publishing:

```bash
./gradlew runPluginVerifier
```

## Publishing the VS Code extension

### Build and install locally

From the repository root:

```bash
cd vscode
npm install
npm run package
code --install-extension angular-files-navigator-1.1.0.vsix
```

Restart or reload VS Code after installation if it was already running. To
remove the locally installed extension:

```bash
code --uninstall-extension vna.angular-files-navigator
```

### Publish to the Visual Studio Marketplace

1. Create or sign in to a Microsoft account at
   <https://marketplace.visualstudio.com/manage>.
2. Create a publisher. The `publisher` value in `vscode/package.json` must
   match that publisher ID. The current value is `vna`; change it if your
   Marketplace publisher uses another ID.
3. Create a Personal Access Token in Azure DevOps with Marketplace
   **Manage** scope. Keep the token private.
4. Log in with `vsce`:

   ```bash
   cd vscode
   npx vsce login vna
   ```

   When prompted, enter the Personal Access Token.
5. Build and publish the package:

   ```bash
   npm run package
   npx vsce publish
   ```

   Or publish and increment the patch version automatically:

   ```bash
   npx vsce publish patch
   ```

The first publication is reviewed by Microsoft before it becomes available.
Subsequent releases require increasing the `version` in `vscode/package.json`
or using `vsce publish patch|minor|major`. Do not commit the PAT or place it
in source control or CI logs.

The VS Code Marketplace introduction is defined by `vscode/README.md`. Keep
the README demo and usage text focused on the search-bar recommendations:
`Ctrl+Alt+A` opens the related-file search bar, arrow keys move through the
suggestions, shortcut letters such as `T`, `U`, and `S` select a role, and
**Enter** opens the selected result.

The demo image uses the HTTPS URL
`https://raw.githubusercontent.com/vunguyen96/angular-files-navigator/main/docs/angular-files-navigator-vscode-demo.gif`.
Push `docs/angular-files-navigator-vscode-demo.gif` to the repository's
`main` branch before publishing; the Marketplace requires README images to be
available from an HTTPS URL and will not render an uncommitted local asset.
