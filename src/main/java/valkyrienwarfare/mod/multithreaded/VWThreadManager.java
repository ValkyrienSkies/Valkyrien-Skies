/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.multithreaded;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.World;

/**
 * Only needs to run for the server end of things, the client end will just
 * receive more physics ticks as a result. Handles separate physics threads for
 * different worlds.
 * 
 * @author thebest108
 *
 */
public class VWThreadManager {

    // Potential memory leak here, always be sure to call the killVWThread() method
    // once a world unloads.
    private static final Map<World, VWThread> WORLDS_TO_THREADS = new HashMap<World, VWThread>();

    public static VWThread getVWThreadForWorld(World world) {
        return WORLDS_TO_THREADS.get(world);
    }

    public static void createVWThreadForWorld(World world) {
        WORLDS_TO_THREADS.put(world, new VWThread(world));
        WORLDS_TO_THREADS.get(world).start();
    }

    public static void killVWThreadForWorld(World world) {
        WORLDS_TO_THREADS.remove(world).kill();
    }
}
