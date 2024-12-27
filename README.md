# Ksyxis

Speed up your world loading by removing spawn chunks.

## How?

*1.20.4 and older*: Vanilla Minecraft loads 21x21 (441) spawn chunks every time you load a world.  
*1.20.5 and newer*: Vanilla Minecraft loads 5x5 (25) spawn chunks every time you load a world.

**This mod fully removes spawn chunks.**

## Dependencies

**Fabric**: (none)  
**Forge 1.16 and newer**: (none)  
**Forge 1.15.2 and older**: Any Mixin provider at your choice (such as
[MixinBootstrap](https://modrinth.com/mod/mixinbootstrap), [MixinBooter](https://modrinth.com/mod/mixinbooter),
[UniMixins](https://modrinth.com/mod/unimixins) or any other)  
**NeoForge**: (none)  
**Quilt**: (none)  
**Legacy Fabric**: (none)  
**Ornithe** (none)

## FAQ

**Q**: I need help, have questions, or something else.  
**A**: You can join the [Discord server](https://discord.gg/Q6saSVSuYQ).

**Q**: Where can I download this mod?  
**A**: [Modrinth](https://modrinth.com/mod/ksyxis),
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/ksyxis),
[GitHub](https://github.com/VidTu/Ksyxis).
You can also find unstable builds at [GitHub Actions](https://github.com/VidTu/Ksyxis/actions),
you'll need a GitHub account to download them.

**Q**: This mod doesn't speed up anything.  
**A**: The effect may not be noticeable on high-end PCs. This mod is designed primarily for low-end devices.
There's [a video](https://www.youtube.com/watch?v=PXWdDoVU1C4) through.

**Q**: Which mod loaders are supported?  
**A**: Forge, Fabric, Quilt, NeoForge, Legacy Fabric, Ornithe (for both Fabric and Quilt).

**Q**: Which versions are supported?  
**A**: Minecraft 1.8 or newer. There may be a backport to older versions in the future.

**Q**: Where are the Forge, Fabric, Quilt, NeoForge, etc. versions?  
**A**: All in the same file.

**Q**: Do I need Fabric API or Quilt Standard Libraries?  
**A**: No, but you can have it installed for other mods.

**Q**: Is this mod open source?  
**A**: [Yes.](https://github.com/VidTu/Ksyxis) (Licensed
under [MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE))

**Q**: Is this mod stable for use?  
**A**: It should be. No guarantee though. At least it should not break your worlds, you just won't be able to load them
with this mod and can uninstall it to load otherwise.

**Q**: Is this mod client-side or server-side?  
**A**: This mod works on logical server side. That is, it has effect in singleplayer and
on a dedicated (standalone) server. It has no effect on the client when playing in multiplayer.
You can install it into your client or your server without any requirements for it to be installed on the other side.

**Q**: How to force-load chunks for ticking if spawn chunks have been removed?  
**A**: If you really need ticking chunks, load individual chunks with the `/forceload` vanilla command in 1.13 or newer.
For older versions, you can search for some mod that force-loads chunks.

**Q**: I've found a bug.  
**A**: Report it [here](https://github.com/VidTu/Ksyxis/issues). If you are not sure if this is a bug, you can join
the [Discord](https://discord.gg/Q6saSVSuYQ).

**Q**: Can I use this in my modpack?  
**A**: Sure. Credit (e.g. a link to mod's GitHub page) is appreciated, but is not required.
Monetization and redistributing is allowed as per the [MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE).

**Q**: It says *Ksyxis: No Mixin found*.  
**A**: If you're using ancient Forge (1.15.2 or older), you may need to
install [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap), [MixinBooter](https://modrinth.com/mod/mixinbooter),
[UniMixins](https://modrinth.com/mod/unimixins) **OR** any other at your choice. If you're using Forge 1.16 or newer,
any Fabric, any Quilt, or any NeoForge, you don't need to install anything and this is a bug.

## License

This project is provided under the MIT License.
Check out [LICENSE](https://github.com/VidTu/Ksyxis/blob/main/LICENSE) for more information.

## Building

1. Have 1 GB of free RAM, 1 GB of free disk space, and an active internet connection.
2. Install Java 8 or newer<sup>*</sup> and dump it into PATH and/or JAVA_HOME.
3. Run `./gradlew build` from the terminal/PowerShell.
4. Grab the JAR from the `./build/libs/` folder.

<sup>* Actual Java version used currently is 8, Gradle will automatically download it.</sup>
