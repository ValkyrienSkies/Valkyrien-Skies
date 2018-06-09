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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.collision.optimization.ShipCollisionTask;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.physics.management.WorldPhysObjectManager;

/**
 * Handles all the physics processing for a world separate from the game tick.
 * Currently filled with tons of synchronization issues.
 *
 * @author thebest108
 */
public class VWThread extends Thread {

	private final static long NS_PER_TICK = 10000000;
	private final static long MAX_LOST_TIME_NS = 1000000000;
	// The number of physics ticks to be considered in the average tick time.
	private final static long TICK_TIME_QUEUE = 100;
	// Used to give each VW thread a unique name
	private static int threadID = 0;
	private final World hostWorld;
	// The ships we will be ticking physics for every tick, and sending those
	// updates to players.
	private final List<PhysicsWrapperEntity> ships;
	private int physicsTicksCount;
	// Used by the game thread to mark this thread for death.
	private volatile boolean threadRunning;
	private final Queue<Long> latestPhysicsTickTimes;

	public VWThread(World host) {
		super("VW World Thread " + threadID);
		threadID++;
		this.hostWorld = host;
		this.ships = new ArrayList<PhysicsWrapperEntity>();
		this.physicsTicksCount = 0;
		this.threadRunning = true;
		this.latestPhysicsTickTimes = new ConcurrentLinkedQueue<Long>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// System.out.println("Thread running");
		// Used to make up for any lost time when we tick
		long lostTickTime = 0;
		while (threadRunning) {
			long startOfPhysicsTickTimeNano = System.nanoTime();
			// Limit the tick smoothing to just one second (1000ms), if lostTickTime becomes
			// too large then physics would move too quickly after the lag source was
			// removed.
			if (lostTickTime > MAX_LOST_TIME_NS) {
				lostTickTime %= MAX_LOST_TIME_NS;
			}
			// Run the physics code
			runGameLoop();
			long endOfPhysicsTickTimeNano = System.nanoTime();
			long deltaPhysicsTickTimeNano = endOfPhysicsTickTimeNano - startOfPhysicsTickTimeNano;

			try {
				long sleepTime = NS_PER_TICK - deltaPhysicsTickTimeNano;
				// Sending a negative sleepTime would crash the thread.
				if (sleepTime > 0) {
					// If our lostTickTime is greater than zero then we're behind a few ticks, try
					// to make up for it by skipping sleep() time.
					if (sleepTime > lostTickTime) {
						sleepTime -= lostTickTime;
						lostTickTime = 0;
						sleep(sleepTime / 1000000L);
					} else {
						lostTickTime -= sleepTime;
					}
				} else {
					// We were late in processing this tick, add it to the lost tick time.
					lostTickTime -= sleepTime;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			long endOfTickTimeFullNano = System.nanoTime();
			long deltaTickTimeFullNano = endOfTickTimeFullNano - startOfPhysicsTickTimeNano;

			// Update the average tick time here:
			latestPhysicsTickTimes.add(deltaTickTimeFullNano);
			if (latestPhysicsTickTimes.size() > TICK_TIME_QUEUE) {
				// Remove the head of this queue.
				latestPhysicsTickTimes.poll();
			}
		}
		// If we get to this point of run(), then we are about to return and this thread
		// will terminate.
		System.out.println(super.getName() + " killed");
	}

	private void runGameLoop() {
		MinecraftServer mcServer = hostWorld.getMinecraftServer();
		if (mcServer.isServerRunning()) {
			if (mcServer.isDedicatedServer()) {
				// Always tick the physics
				physicsTick();
				tickSendUpdatesToPlayers();
			} else {
				// Only tick the physics if the game isn't paused
				if (!isSinglePlayerPaused()) {
					physicsTick();
					tickSendUpdatesToPlayers();
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private static boolean isSinglePlayerPaused() {
		return Minecraft.getMinecraft().isGamePaused();
	}

	// The whole time need to be careful the game thread isn't messing with these
	// values.
	private void physicsTick() {
		// TODO: Temporary fix:
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(hostWorld);
		List<PhysicsWrapperEntity> physicsEntities = manager.getTickablePhysicsEntities();
		ships.clear();
		ships.addAll(physicsEntities);
		// System.out.println(ships.size());
		tickThePhysicsAndCollision();
		tickTheTransformUpdates();
	}

	private void tickThePhysicsAndCollision() {
		double newPhysSpeed = ValkyrienWarfareMod.physSpeed;
		Vector newGravity = ValkyrienWarfareMod.gravity;
		List<ShipCollisionTask> collisionTasks = new ArrayList<ShipCollisionTask>(ships.size() * 2);

		for (PhysicsWrapperEntity wrapper : ships) {
			if (!wrapper.firstUpdate) {
				// Update the physics simulation
				wrapper.getPhysicsObject().getPhysicsProcessor().rawPhysTickPreCol(newPhysSpeed);
				if (wrapper.getPhysicsObject().isPhysicsEnabled()) {
					// Update the collision task if necessary
					wrapper.getPhysicsObject().getPhysicsProcessor().getWorldCollision().tickUpdatingTheCollisionCache();
					// Take the big collision and split into tiny ones
					wrapper.getPhysicsObject().getPhysicsProcessor().getWorldCollision()
							.splitIntoCollisionTasks(collisionTasks);
				}
			}
		}

		try {
			// The individual collision tasks will sort through a lot of data to find
			// collision points
			ValkyrienWarfareMod.PHYSICS_THREADS_EXECUTOR.invokeAll(collisionTasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Then those collision points have to be processed sequentially afterwards, all in
		// this thread. Thankfully this step is not cpu intensive.
		for (ShipCollisionTask task : collisionTasks) {
			PhysicsWrapperEntity wrapper = task.getToTask().getParent().getWrapperEntity();
			if (!wrapper.firstUpdate) {
				task.getToTask().processCollisionTask(task);
			}
		}
	}

	// TODO: Try to synchronize this better with the main game thread, otherwise we
	// could end up with instability.
	private void tickTheTransformUpdates() {
		for (PhysicsWrapperEntity wrapper : ships) {
			if (!wrapper.firstUpdate) {
				try {
					wrapper.getPhysicsObject().getPhysicsProcessor().rawPhysTickPostCol();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				wrapper.getPhysicsObject().getShipTransformationManager().updateAllTransforms(false, false);
			}
		}
	}

	private void tickSendUpdatesToPlayers() {
		for (PhysicsWrapperEntity wrapper : ships) {
			// if (wrapper.getPhysicsObject().blockPositions.size() > 10000) {
			// System.out.println(wrapper.getPhysicsObject().blockPositions.size());
			// }
			wrapper.getPhysicsObject().getShipTransformationManager().sendPositionToPlayers(physicsTicksCount);
		}
		physicsTicksCount++;
	}

	/**
	 * Marks this physics thread for death. Doesn't immediately end the thread, but
	 * instead ensures the thread will die after the current running physics tick is
	 * finished.
	 */
	public void kill() {
		System.out.println(super.getName() + " marked for death.");
		threadRunning = false;
	}

	/**
	 * 
	 * @return The average runtime of the last 100 physics ticks in nanoseconds.
	 */
	public long getAveragePhysicsTickTimeNano() {
		if (latestPhysicsTickTimes.size() >= TICK_TIME_QUEUE) {
			long average = 0;
			for (Long tickTime : latestPhysicsTickTimes) {
				average += tickTime;
			}
			return average / TICK_TIME_QUEUE;
		}
		// If we don't have enough data to get an average, just assume its the ideal
		// tick time.
		return NS_PER_TICK;
	}
}
