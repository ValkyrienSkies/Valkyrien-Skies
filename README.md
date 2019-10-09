
# Valkyrien Warfare
[![CircleCI](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies.svg?style=svg)](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies)

See: [LICENSE file](https://github.com/ValkyrienWarfare/Valkyrien-Warfare-Revamped/blob/8778e9d45f16c1f60e8149ab6cbdbabbdebc1278/LICENSE), [Project Roadmap](https://wiki.valkyrienskies.org/wiki/Roadmap), [Wiki Page](https://wiki.valkyrienskies.org/wiki/Main_Page)

The Airships Mod to end all other Airships Mods. Better compatibility, performance, collisions, interactions and physics than anything prior!

*Note: Much of our documentation is now available on the wiki - [wiki.valkyrienskies.org](https://wiki.valkyrienskies.org/wiki/Main_Page) - info on this page may be outdated.*

## Installation

### Downloading
Official and stable releases of the Valkyrien Warfare mod can be found on the [CurseForge page](https://minecraft.curseforge.com/projects/valkyrien-warfare/files).

Beta releases (warning: may be unstable!) can be found on the following:
- the [Jenkins build server](https://jenkins.daporkchop.net/job/Minecraft/job/ValkyrienWarfare/) 
- the [CircleCI](https://circleci.com/gh/ValkyrienWarfare/Valkyrien-Warfare-Revamped/tree/master) (click on the latest build #, then artifacts, then download the mod JAR file)

### Installing on your server
To install Valkyrien Warfare, move the downloaded `.jar` file into your Minecraft's `mods/` folder, just as you would any other mod.

## Development

*You may also be interested in the wiki page on development, which contains detailed instructions, FAQs, and more. https://wiki.valkyrienskies.org/wiki/Dev:Main_Page*

### Eclipse
1. Clone the repo: `git clone https://github.com/ValkyrienWarfare/Valkyrien-Warfare-Revamped`
2. Copy in the `eclipse/` folder from a fresh installation of the [Forge MDK](http://files.minecraftforge.net)
3. Run `./gradlew setupDecompWorkspace eclipse`
4. Open the project in Eclipse

### IntelliJ
1.  Clone the repo: `git clone https://github.com/ValkyrienWarfare/Valkyrien-Warfare-Revamped`
2. Run `./gradlew setupDecompWorkspace idea genIntellijRuns`
3. Open the project in IntelliJ

