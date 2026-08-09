<img alt src=https://github.com/VidTu/Ksyxis/raw/main/docs/ksyxis_dev.png>

# Ksyxis (Dev's Corner)

This is the page with various technical information for the Ksyxis mod.

**Check out the [main page](https://github.com/VidTu/Ksyxis/blob/main/docs/README.md)
if you are not a technical person/not a developer. (and not a nerd)**

## Developer FAQ

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

## Building (Compiling)

To compile the mod from the source code:

1. Have 1 GB of free RAM, 1 GB of free disk space,
   and an active internet connection.
2. Install Java 25 (for Gradle; you'll also need 8 for the compilation,
   download either of those, the other will be automatically downloaded
   via Java toolchains) and dump it into `PATH` and/or `JAVA_HOME`.
3. Clone or download the repository. (`.git` folder is *not* required)
4. Run `./gradlew assemble` from the terminal/PowerShell
   from within the downloaded repository folder.
5. Grab the JAR from the `./build/libs/` folder.

### Developing/Debugging

Due to its Minecraft-less nature, Ksyxis doesn't currently
offer a simple development environment. Good luck!

The recommended IDE for development is IntelliJ IDEA (Community or Ultimate)
with the Minecraft Development plugin. This is not a strict requirement,
however. Any IDE/editor should work just fine.

### Debug JARs

The `ru.vidtu.ksyxis.debug` boolean Gradle property allows producing
more debuggable JARs. It controls the following sub-properties:

- `ru.vidtu.ksyxis.debug.javac`: Tell `javac` to emit `-parameters`
  data for reflection. Reflective data includes method parameter flags
  and names, which might be useful for debugging or decompilation.
- `ru.vidtu.ksyxis.debug.metadata`: Don't use the custom post-processor
  (see the `buildSrc` folder) to strip annotations and other metadata
  from class-files like `SourceFile` attributes and `@deprecated` tags.
- `ru.vidtu.ksyxis.debug.asserts`: Keep `assert` statements after
  compilation for better debuggability. Note that assertions must
  be enabled using `-enableassertions` VM flag on most JVMs.
- `ru.vidtu.ksyxis.debug.logs`: Change logging calls:
  - Enable `debug` and `trace` logging calls and create loggers for them
    in some classes where loggers are not created during normal execution.
  - Add a `MOD_KSYXIS` marker to every logging call.
  - Always produce stack-traces for exceptions.
  - Produce more logging details. (more context, more parameters, etc.)
- `ru.vidtu.ksyxis.debug.resources`: Don't minify resource files that are
  *not* Java classes, such as `.json`, `.toml`, `.mcmeta` files and others.
- `ru.vidtu.ksyxis.debug.package`: Don't strip `package-info.class` files.
- `ru.vidtu.ksyxis.debug.jars`: Create additional two JARs with
  Javadocs and sources. Will cause the `build/libs` folder to split.

More specific debug properties will override the global one.
All debug properties are *disabled* by default. See the
[Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)
for more information on how to set Gradle properties.

For example, you can compile the "JAR with all debug properties"
using `./gradlew -Pru.vidtu.ksyxis.debug=true assemble`.

### Slim JARs

> [!WARNING]
> **Note**: This option *will* make it harder to know what parts of the mod are
> causing issues. Do *not* enable this option unless you know what you're doing!

If you want to produce extra-small JARs at the cost of debuggability, you
can set the `ru.vidtu.ksyxis.slim` boolean Gradle property to `true`.

This property is incompatible with the Debug JARs options.

## Contributing

There are no strict requirements for neither issues nor pull requests.

### Issues

The main requirements for issues are basically the bare minumum. Issues must be:

- Descriptive (have all the necessary info)
- Relevant (related to the project's functionality)
- Understandable (by both you and project's authors)
- In English

### Pull Requests (PRs)

The main requirements for PRs are basically the bare minimum. PRs must be:

- Working (compiling and running on all supported
  versions; preferably they run without bugs)
- Full (have all the necessary info and code in one PR)
- Relevant (related to the project's functionality)
- Understandable (by you and project's authors)
- In English (both the code and the description)

## Security

Check out [SECURITY.md](https://github.com/VidTu/Ksyxis/blob/main/docs/SECURITY.md)
if you're insecure. <3
