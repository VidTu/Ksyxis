<img alt src=ksyxis.png>

# Ksyxis

Speed up your world loading by removing unneeded chunks.

## Language

- **English** 🇬🇧 🇺🇸
- [Русский 🇷🇺](README_ru.md)

## Downloads

- [Modrinth](https://modrinth.com/mod/ksyxis)
- [CurseForge](https://curseforge.com/minecraft/mc-mods/ksyxis)
- [GitHub Releases](https://github.com/VidTu/Ksyxis/releases)

## Dependencies

- Fabric, Forge, NeoForge, Quilt, Legacy Fabric, or Ornithe
- Minecraft (1.8 or newer)
- **Forge 1.8-1.15.2 only**: Any Mixin provider, at your choice (such as
  [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap),
  [MixinBooter](https://modrinth.com/mod/mixinbooter),
  [UniMixins](https://modrinth.com/mod/unimixins), or any other)

## About

Depending on your game version, Minecraft loads some [chunks](https://minecraft.wiki/w/Chunks) when you
create your world. Sometimes, these chunks are always loaded in the background. Either way, whether
these chunks being loaded is a one-time performance slowdown, or a constant performance penalty,
most players don't need any of these chunks. This mod completely disables unneeded chunks in the game.

*Note*: Unneeded chunks are sometimes used by farms and technical contraptions. If you'll
need these chunks, you can always delete the mod later to re-enable these chunks.

https://github.com/user-attachments/assets/42e65893-6324-46b1-89a4-044eae77802d

## Versions

| Version       | Effect        | Note                                                                                                                                           |
|---------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.21.9+       | Insignificant | A 7x7 area of chunks won't be loaded around the player when they join, and a fake 500ms world creation delay in single-player will be removed. |
| 1.20.5-1.21.8 | Low           | A 5x5 area of spawn chunks won't be loaded constantly in the background.                                                                       |
| 1.8-1.20.4    | Extreme       | A 21x21 area of spawn chunks won't be loaded constantly in the background.                                                                     |

## FAQ

### For Players

**Q**: I need help, have some questions, or have some other feedback.  
**A**: You can join the [Discord server](https://discord.gg/Q6saSVSuYQ).

**Q**: Where can I download this mod?  
**A**: [Modrinth](https://modrinth.com/mod/ksyxis),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/ksyxis),
or [GitHub Releases](https://github.com/VidTu/Ksyxis/releases).
You can also find unstable builds at
[GitHub Actions](https://github.com/VidTu/Ksyxis/actions).
You will need a GitHub account to download these.

**Q**: Which mod loaders are supported?  
**A**: Fabric, Forge, NeoForge, Quilt, Legacy Fabric, and Ornithe.

**Q**: Which Minecraft versions are supported?  
**A**: Minecraft versions 1.8 through 1.21.8 are supported.
Minecraft versions 1.21.9 and newer are **not** supported.

**Q**: Why support so many Minecraft versions?  
**A**: Because I can.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: Not necessarily.

**Q**: Where are the Fabric, Forge, NeoForge, Quilt, etc. versions?  
**A**: All in the same file.

**Q**: Is this mod client-side or server-side?  
**A**: This mod works on the server and on the client in singleplayer.
It has no effect on the client when playing multiplayer.

**Q**: Is this mod stable for use?  
**A**: Should be fine. If it breaks something,
just delete it and your worlds will be fine.

**Q**: I've found a bug.  
**A**: Report it [here](https://github.com/VidTu/Ksyxis/issues). If you are not
sure whether this is a bug or a simple question, you can join the
[Discord](https://discord.gg/Q6saSVSuYQ). Report security vulnerabilities
[here](https://github.com/VidTu/Ksyxis/security).

**Q**: Can I use this in my modpack?  
**A**: Sure. Credit (e.g., a link to the mod's GitHub page) is appreciated but
is not required. Monetization and redistribution are allowed as per the
[MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE).

**Q**: This mod doesn't speed up anything.  
**A**: The effect may not be noticeable on high-end PCs. This mod
is designed primarily for low-end devices. Nevertheless, there is
[a video](https://www.youtube.com/watch?v=PXWdDoVU1C4).

**Q**: How to force-load chunks if the spawn chunks have been removed?  
**A**: If you really need to force-load chunks, load individual
chunks with the `/forceload` command in 1.13 or newer. For older
versions, you can search for some mod that force-loads chunks.

**Q**: It says *Ksyxis: No Mixin found*.  
**A**: If you're using Forge 1.15.2 or older, you may need to install
[MixinBootstrap](https://modrinth.com/mod/mixinbootstrap),
[MixinBooter](https://modrinth.com/mod/mixinbooter),
[UniMixins](https://modrinth.com/mod/unimixins), or any other
Mixin provider of your choice. If you're using Forge 1.16
or newer, or any version of Fabric/NeoForge/Quilt/Ornithe,
you don't need to install anything, and this is a bug.

### For Developers

**Q**: Is this mod open source?  
**A**: [Yes.](https://github.com/VidTu/Ksyxis) (Licensed
under [MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE))

**Q**: Why so much yapping in this README?  
**A**: ~~I paid for the whole LLM, I'm going to use the whole LLM.~~
Because writing READMEs is easier than writing actual code.

**Q**: Do you use AI/LLM/Code Generation/Copilot/etc.?  
**A**: No, the code is 99.67% human-written, because AI is generating dumb stuff,
especially for Minecraft. If you (or some contributors) will use AI, and it will
magically® work™, good for you. I don't promote AI nor am I against it.

**Q**: Does Ksyxis have a public API?  
**A**: Nope. There's no public-facing API in this mod.
All classes/packages are marked as
[@ApiStatus.Internal](https://javadoc.io/static/org.jetbrains/annotations/26.1.0/org/jetbrains/annotations/ApiStatus.Internal.html)
for that reason.

**Q**: Can I still *link*/compile against to the mod? What about
the [SemVer](https://semver.org/) versioning used by the mod?  
**A**: You can, at your own risk. SemVer-compatible versioning is used
by Ksyxis for ease of use, but it is used arbitrarily. This mod
does not declare a public API, therefore, breaking source/binary
changes may and will occur even between minor and patch versions.

**Q**: Why use these shenanigans with Minecraft-less build when
it's much easier to develop and test with Minecraft present?  
**A**: When this was developed, I had a goal of targeting
as many versions and loaders as possible using Mixin and
nothing else. This is a bad idea, but it is what it is.

## License

This project is provided under the MIT License.
Check out [LICENSE](https://github.com/VidTu/Ksyxis/blob/main/LICENSE)
for more information.

## Credits

This mod is developed primarily by [VidTu](https://github.com/VidTu),
but it wouldn't be possible without:

- [Contributors](https://github.com/VidTu/Ksyxis/graphs/contributors).
- [Blossom](https://github.com/KyoriPowered/blossom) by
  [Kyori](https://github.com/KyoriPowered). (and contributors)
- [Fabric Loader](https://github.com/FabricMC/fabric-loader) by
  [FabricMC](https://github.com/FabricMC). (and contributors)
- [NeoForge](https://github.com/neoforged/NeoForge) by
  [NeoForged](https://github.com/neoforged). (and contributors)
- [Forge](https://github.com/MinecraftForge/MinecraftForge) by
  [Minecraft Forge](https://github.com/MinecraftForge). (and contributors)
- [Mixin](https://github.com/SpongePowered/Mixin) by
  [SpongePowered](https://github.com/SpongePowered). (and contributors)
- [Minecraft](https://minecraft.net/) by
  [Mojang](https://mojang.com/).

It also uses [Gradle](https://gradle.org/) and [Java](https://java.com/).

## Development

### Building (Compiling)

To compile the mod from the source code:

1. Have 1 GB of free RAM, 1 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 8 for the compilation,
   download either of those, the other will be automatically downloaded
   via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Run `./gradlew assemble` from the terminal/PowerShell.
4. Grab the JAR from the `./build/libs/` folder.

### Developing/Debugging

Due to its Minecraft-less nature, Ksyxis doesn't currently
offer a simple development environment. Good luck!

The recommended IDE for development is IntelliJ IDEA (Community or Ultimate)
with the Minecraft Development plugin. This is not a strict requirement,
however. Any IDE/editor should work just fine.
