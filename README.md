# Ksyxis

Speed up your world loading by removing spawn chunks.

## Downloads

- [Modrinth](https://modrinth.com/mod/ksyxis)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ksyxis)
- [GitHub Releases](https://github.com/VidTu/Ksyxis/releases)

## Dependencies

This mod needs *Mixin* and nothing else. Usually, it's provided by your mod loader.

- Fabric Loader, Quilt Loader, Forge, NeoForge, Legacy Fabric, or Ornithe
- Minecraft (1.8 or newer)
- **Forge 1.8-1.15.2 only**: Any Mixin provider, at your choice (such as
  [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap), [MixinBooter](https://modrinth.com/mod/mixinbooter),
  [UniMixins](https://modrinth.com/mod/unimixins), or any other)

Fabric API is **NOT** required.

## About

Minecraft has a concept of [spawn chunks](https://minecraft.wiki/w/Spawn_chunk). These chunks are located at the
world creation point (where you have been spawned for the first time) and are always loaded. Depending on the version,
there are either 441 or 25 spawn chunks always loaded while you're playing. Most players, however, don't need
these chunks as they venture far away from spawn and come back only occasionally. To these players, the creation
and loading of spawn chunks is a waste of time and performance. This mod completely disables spawn chunks in the game.

*Note*: Spawn chunks are sometimes used by farms and technical contraptions. If you'll need them,
you can always delete the mod later to re-enable the spawn chunks.

## FAQ

**Q**: I need help, have some questions, or have some other feedback.  
**A**: You can join the [Discord server](https://discord.gg/Q6saSVSuYQ).

**Q**: Where can I download this mod?  
**A**: [Modrinth](https://modrinth.com/mod/ksyxis),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/ksyxis),
or [GitHub Releases](https://github.com/VidTu/Ksyxis/releases).
You can also find unstable builds at [GitHub Actions](https://github.com/VidTu/Ksyxis/actions).
You will need a GitHub account to download these.

**Q**: This mod doesn't speed up anything.  
**A**: The effect may not be noticeable on high-end PCs. This mod is designed primarily for low-end devices.
Nevertheless, there is [a video](https://www.youtube.com/watch?v=PXWdDoVU1C4).

**Q**: Which mod loaders are supported?  
**A**: Forge, Fabric, Quilt, NeoForge, Legacy Fabric, and Ornithe (for both Fabric and Quilt).

**Q**: Which Minecraft versions are supported?  
**A**: Minecraft 1.8 or newer.

**Q**: Where are the Forge, Fabric, Quilt, NeoForge, etc. versions?  
**A**: All in the same file.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: No, but you can install these for other mods.

**Q**: Is this mod open source?  
**A**: [Yes.](https://github.com/VidTu/Ksyxis) (Licensed
under [MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE))

**Q**: Is this mod stable for use?  
**A**: It should be. No guarantee, though. At least it should not break your worlds.
You can always uninstall the mod and load worlds without Ksyxis.

**Q**: Is this mod client-side or server-side?  
**A**: This mod works on the logical server side. That is, it does have an effect in singleplayer and
on a dedicated (standalone) server. It has no effect on the client when playing in multiplayer.
You can install it into your client or your server without any requirements for it to be installed on the other side.

**Q**: How to force-load chunks if the spawn chunks have been removed?  
**A**: If you really need to force-load chunks, load individual chunks with the `/forceload`
command in 1.13 or newer. For older versions, you can search for some mod that force-loads chunks.

**Q**: I've found a bug.  
**A**: Report it [here](https://github.com/VidTu/Ksyxis/issues). If you are not sure whether this is a bug or a
simple question, you can join the [Discord](https://discord.gg/Q6saSVSuYQ).
Report security vulnerabilities [here](https://github.com/VidTu/Ksyxis/security).

**Q**: Can I use this in my modpack?  
**A**: Sure. Credit (e.g., a link to the mod's GitHub page) is appreciated but is not required.
Monetization and redistribution are allowed as per the [MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE).

**Q**: It says *Ksyxis: No Mixin found*.  
**A**: If you're using Forge 1.15.2 or older, you may need to install
[MixinBootstrap](https://modrinth.com/mod/mixinbootstrap), [MixinBooter](https://modrinth.com/mod/mixinbooter),
[UniMixins](https://modrinth.com/mod/unimixins), or any other Mixin provider of your choice. If you're using Forge 1.16
or newer, or any version of Fabric/Quilt/NeoForge/Ornithe, you don't need to install anything, and this is a bug.

## License

This project is provided under the MIT License.
Check out [LICENSE](https://github.com/VidTu/Ksyxis/blob/main/LICENSE) for more information.

## Development

### Building (Compiling)

To compile the mod from the source code:

1. Have 1 GB of free RAM, 1 GB of free disk space, and an active internet connection.
2. Install Java 8 and dump it into PATH and/or JAVA_HOME.
3. Run `./gradlew build` from the terminal/PowerShell.
4. Grab the JAR from the `./build/libs/` folder.

### Developing/Debugging

Due to its multiplatform/multiversion nature and general code simplicity,
Ksyxis doesn't currently offer a comprehensive development environment.

### Reproducible Builds

Ksyxis attempts to have reproducible builds (reproducible JAR archives) for its releases. Check out
[GitHub Releases](https://github.com/VidTu/Ksyxis/releases) for "Reproducible Build Hash" values. If it is present
on any release, this release's binary JAR should be reproducible. Unfortunately, due to the nature of Java
(Gradle) and Minecraft development, it is not always possible to have reproducible builds.
Reproducible release JARs are compiled with: (use these commands to create a reproducible build)

```bash
./gradlew clean --no-daemon --no-build-cache --no-configuration-cache
./gradlew build --no-daemon --no-build-cache --no-configuration-cache
```

Currently, no dependency (integrity) validation is performed. This might change in a future version.
