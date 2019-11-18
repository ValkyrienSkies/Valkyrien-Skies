
# Valkyrien Skies
[![CircleCI](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies.svg?style=svg)](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies)

See: [LICENSE file](https://github.com/ValkyrienSkies/Valkyrien-Skies/blob/8778e9d45f16c1f60e8149ab6cbdbabbdebc1278/LICENSE), [Project Roadmap](https://github.com/ValkyrienSkies/Valkyrien-Skies/wiki/Roadmap)

The Airships Mod to end all other Airships Mods. Better compatibility, performance, collisions, interactions and physics than anything prior!

## Installation

### Downloading
Official and stable releases of the Valkyrien Skies mod can be found on the [CurseForge page](https://minecraft.curseforge.com/projects/valkyrien-skies/files).

Beta releases (warning: may be unstable!) can be found on the following:
- the [Jenkins build server](https://jenkins.daporkchop.net/job/Minecraft/job/ValkyrienWarfare/)
- the [CircleCI](https://circleci.com/gh/ValkyrienSkies/Valkyrien-Skies/tree/master) (click on the latest build #, then artifacts, then download the mod JAR file)

### Installing on your server
To install Valkyrien Skies, move the downloaded `.jar` file into your Minecraft's `mods/` folder, just as you would any other mod.

## Wiki

Lots of information, including tutorials, can be found [on the Wiki](https://wiki.valkyrienskies.org).
Everything there is for the latest version, for old 0.9 builds see below.

## Usage for old versions

### Make a flying ship
1. Build a structure somewhere in the air, not connected to the ground.
2. Place a [**Physics Infuser**] on it.
3. Right click the [**Physics Infuser**] (the ship should fall).
4. Place a [**Pilot Chair**] on the ship.
5. Right click the [**Pilot Chair**] to get in, and then use <kbd>W</kbd>, <kbd>A</kbd>, <kbd>S</kbd>, <kbd>D</kbd>, <kbd>X</kbd>, and <kbd>Space</kbd> to navigate.

### Make your ship hover without a pilot

1. Place (multiple) [**Ether Compressor**] on your ship
2. Place a [**Hovercraft Controller**] on your ship.
3. Right click the [**Hovercraft Controller**] with [**System Linker**].
4. Right click all [**Ether Compressor**] with [**System Linker**].
5. Right click the [**Hovercraft Controller**] with your hand and set the **Hover Height Target** at the Y-level you wish for your ship to hover at.

## Development

### Eclipse
1. Clone the repo: `git clone https://github.com/ValkyrienSkies/Valkyrien-Skies`
2. Copy in the `eclipse/` folder from a fresh installation of the [Forge MDK](http://files.minecraftforge.net)
3. Run `./gradlew setupDecompWorkspace eclipse`
4. Open the project in Eclipse

### IntelliJ
1.  Clone the repo: `git clone https://github.com/ValkyrienSkies/Valkyrien-Skies`
2. Run `./gradlew setupDecompWorkspace idea genIntellijRuns`
3. Open the project in IntelliJ