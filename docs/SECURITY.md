# Security Policy

## Vulnerabilities

### Reporting

Ksyxis is a Minecraft mod. Proper security in this realm is a rare occasion.
Additionally, the niche/role of Ksyxis isn't very prone to vulnerabilities.
However, if you think the bug you have found is a vulnerability,
you can report it privately via any of the following methods:

- GitHub Private vulnerability reporting: Head over to the
  [Security](https://github.com/VidTu/Ksyxis/security)
  tab and click "*Report a vulnerability*".
- Mail: `imvidtu <at> proton <dot> me`

Where possible, **prefer** GitHub Private vulnerability reporting.

There are no public keys (e.g, PGP) to encrypt communication, sorry.[^1]

[^1]: PGP encryption is on my TODO/TBD list. If you really want
      to use it,you should use the same public key I sign my
      commits with. It is *not* uploaded to any public keyserver.

### Supported Versions

The only supported versions for vulnerability reporting are:

- The latest release published to Modrinth, CurseForge, and/or GitHub.
- The latest pre-release published to GitHub[^2].
- The latest alpha and/or beta published to Modrinth and/or GitHub[^2].
- The latest Git commit build.

[^2]: Pre-release, alpha and beta versions are supported *only*
      if they were published after the latest *stable* release.

## Artifacts (Binaries/JARs)

### Reproducible Builds

Status of reproducible builds/binaries:

- \>=[1.4.0](https://github.com/VidTu/Ksyxis/releases/tag/1.4.0):
  All builds should be fully reproducible.
- \>=[1.3.3](https://github.com/VidTu/Ksyxis/releases/tag/1.3.3)
  \<=[1.3.4](https://github.com/VidTu/Ksyxis/releases/tag/1.3.4):
  All builds require `umask` to be `022` to be reproducible.
- \<=[1.3.2](https://github.com/VidTu/Ksyxis/releases/tag/1.3.2):
  Builds are *not* reproducible, modification timestamps are stored in JARs.

For better results, every release should be compiled with:

```sh
./gradlew clean assemble --no-daemon --no-build-cache --no-configuration-cache --rerun-tasks --refresh-dependencies
```

### Signing

Ksyxis is not signed by digital signatures (namely PGP). Sorry![^3]

[^3]: PGP signing is on my TODO/TBD list.

### Supply Chain

Ksyxis has implemented supply chain validation where possible:

- Gradle Wrapper (`gradle/wrapper/gradle-wrapper.jar`) is
  verified on [GitHub CI](../.github/workflows) on every
  commit by `gradle/actions/setup-gradle` action. Verify
  [it](https://gradle.org/release-checksums/) locally too.
- Gradle distribution is verified by the wrapper via `distributionSha256Sum` in
  [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties)
  file after downloading.
- All Gradle plugins and dependencies used are verified via hashes/checksums in
  [verification-metadata.xml](../gradle/verification-metadata.xml). To avoid
  fetching keys for signatures, the local PGP keystore is used from armored
  [verification-keyring.keys](../gradle/verification-keyring.keys). You can
  disable or replace the keyring if you want to use your own pubkey keystore.
- All [GitHub CI](../.github/workflows) workflows are SHA-pinned.

However, Gradle Java Toolchains and Foojay Disco API Resolver
are missing supply-chain verification. Therefore, you must
install Java 8 and Java 25 from your preferred vendors and
[disable auto-provisioning](https://docs.gradle.org/current/userguide/toolchains.html#sub:disable_auto_provision).
You can disable it per-repository (in `./gradle.properties` file), per-build (by
using `./gradlew -Dorg.gradle.java.installations.auto-download=false [...]`),
or system-wide (in `$GRADLE_HOME/gradle.properties` file), at your choice.
