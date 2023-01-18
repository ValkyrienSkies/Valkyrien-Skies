**Valkyrien Skies 1 is now EOL, but it lives on with a superior engine and better quality, performance, and features in the [Valkyrien Skies 2](https://github.com/ValkyrienSkies/Valkyrien-Skies-2) repository.** Join the discord if interested in backporting VS2 to 1.12.


# <img src="vs_logo.png" width="48" height="48">alkyrien Skies
[![CircleCI](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies.svg?style=svg)](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies)
[![Jenkins](https://jenkins.daporkchop.net/job/ValkyrienSkies/job/Valkyrien-Skies/job/master/badge/icon)](https://jenkins.daporkchop.net/job/ValkyrienSkies/job/Valkyrien-Skies/)
[![Discord](https://img.shields.io/discord/244934352092397568.svg)](https://discord.gg/rG3QNDV)

See: [LICENSE file](https://github.com/ValkyrienSkies/Valkyrien-Skies/blob/master/LICENSE), [Project Roadmap](https://github.com/ValkyrienSkies/Valkyrien-Skies/wiki/Roadmap)

The Airships Mod to end all other Airships Mods. Better compatibility, performance, collisions, interactions and physics than anything prior!

## Installation

### Downloading
Official and stable releases of the Valkyrien Skies mod can be found on the [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/valkyrien-skies).

Beta releases (warning: may be unstable!) can be found on the following:
- [CircleCI](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies/tree/master) (click on the latest build #, then artifacts, then download the mod `.jar` file)
- [Jenkins](https://jenkins.daporkchop.net/job/Minecraft/job/ValkyrienSkies/) (click on the branch name, then download the mod `.jar` file)

### Installing on your server
To install Valkyrien Skies, move the downloaded `.jar` file into your Minecraft's `mods/` folder, just as you would any other mod.

## Wiki

Lots of information, including tutorials, can be found [on the Wiki](https://wiki.valkyrienskies.org).
Everything there is for the latest version, for old 0.9 builds see below.

## Development

*You may also be interested in the wiki page on development, which contains detailed instructions, FAQs, and more. https://wiki.valkyrienskies.org/wiki/Dev:Main_Page*

### Eclipse
1. Clone the repo: `git clone --recurse-submodules https://github.com/ValkyrienSkies/Valkyrien-Skies`
2. Copy in the `eclipse/` folder from a fresh installation of the [Forge MDK](http://files.minecraftforge.net)
3. Run `./gradlew setupDecompWorkspace eclipse`
4. Open the project in Eclipse
5. (Optional) recommend `git config submodule.recurse true`

### IntelliJ
1.  Clone the repo: `git clone --recurse-submodules https://github.com/ValkyrienSkies/Valkyrien-Skies`
2. Run `./gradlew setupDecompWorkspace idea genIntellijRuns`
3. Open the project in IntelliJ
4. Import the gradle project, sync gradle
5. Open settings (Control + Alt + S), and search for `Annotation Processors`
6. Check the `Enable annotation processing box`, and add `lombok.launch.AnnotationProcessorHider$AnnotationProcessor` to the list of Processor FQ Names
5. (Optional) recommend `git config submodule.recurse true`

