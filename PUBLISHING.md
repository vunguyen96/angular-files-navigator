# Publishing Angular Files Navigator to the JetBrains Marketplace

## 1. One-time preparation

1. **Pick a unique plugin ID.** The ID in `src/main/resources/META-INF/plugin.xml`
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
3. Upload the ZIP generated in `build/distributions/`.
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

Expose them to Gradle via environment variables (already wired in `build.gradle.kts`):

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
   `build.gradle.kts` for each release and update `<change-notes>` in `plugin.xml`.

## 5. Compatibility range

`patchPluginXml` sets `since-build = 233` (2023.3) and leaves the upper bound open,
so the plugin stays compatible with current and future IDE builds. Set an explicit
`untilBuild` in `build.gradle.kts` if you ever need to cap it.
Run the official compatibility check before publishing:

```bash
./gradlew runPluginVerifier
```
