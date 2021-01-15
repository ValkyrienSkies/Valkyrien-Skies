package org.valkyrienskies.mod.common.util.multithreaded;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.collision.ShipCollisionTask;
import org.valkyrienskies.mod.common.collision.WaterForcesTask;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.network.ShipTransformUpdateMessage;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles the physics for a given world. This is run on a separate thread, not on the game tick.
 */
@Log4j2
public class VSWorldPhysicsLoop implements Runnable {

    // The number of physics ticks to be considered in the average tick time.
    private final static long TICK_TIME_QUEUE = 100;
    // Used to give each VS thread a unique name
    private static int worldPhysicsLoopId = 0;
    private final World hostWorld;
    private final Queue<Long> latestPhysicsTickTimes;
    // The ships we will be ticking physics for every tick, and sending those
    // updates to players.
    // Used by the game thread to mark this thread for death.
    private volatile boolean threadRunning;

    private final Queue<Runnable> taskQueue;
    private ImmutableList<PhysicsObject> immutableShipsList;
    private final ConcurrentLinkedQueue<IPhysTimeTask> recurringTasks;

    @Getter
    private final String name;

    public VSWorldPhysicsLoop(World host) {
        name = "VS World Physics Task " + worldPhysicsLoopId;
        worldPhysicsLoopId++;
        this.hostWorld = host;
        this.threadRunning = true;
        this.latestPhysicsTickTimes = new ConcurrentLinkedQueue<>();
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.immutableShipsList = ImmutableList.of();
        this.recurringTasks = new ConcurrentLinkedQueue<>();
        log.trace(name + " created.");
    }

    @SideOnly(Side.CLIENT)
    private static boolean isSinglePlayerPaused() {
        return Minecraft.getMinecraft().isGamePaused();
    }

    public void addScheduledTask(Runnable r) {
        taskQueue.add(r);
    }

    private static long getNsPerTick() {
        return (long) (1_000_000_000 / VSConfig.targetTps);
    }

    public void addRecurringTask(IPhysTimeTask physTask) {
        recurringTasks.add(physTask);
    }
    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (threadRunning) {
            final MinecraftServer mcServer = hostWorld.getMinecraftServer();
            assert mcServer != null;
            // If server then always tick physics, if single-player then only tick when not paused.
            final boolean tickPhysics = mcServer.isServerRunning() && (mcServer.isDedicatedServer() || !isSinglePlayerPaused());

            if (tickPhysics) {
                // The number of seconds the physics engine will move forward
                final double timeToSimulate = VSConfig.getTimeSimulatedPerTick();
                // The number of nanoseconds we want our physics engine tick to take
                final long idealTickTime = (long) (1E9 / VSConfig.targetTps);

                final long physTickStartTime = System.nanoTime();
                // Run the physics engine tick
                physicsTick(timeToSimulate);
                final long physTickEndTime = System.nanoTime();
                final long physTickDuration = physTickEndTime - physTickStartTime;

                // If the physics tick ran faster than the ideal tick time, then pretend it took the ideal tick time by
                // waiting.
                if (physTickDuration < idealTickTime) {
                    final long sleepMillis = (idealTickTime - physTickDuration) / 1_000_000L;
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Keep track of the time it took to run the physics tick, including the time we spent sleeping.
                final long physTickDurationIncludingSleep = System.nanoTime() - physTickStartTime;
                latestPhysicsTickTimes.add(physTickDurationIncludingSleep);
                // Ensure that latestPhysicsTickTimes only has TICK_TIME_QUEUE # of elements
                if (latestPhysicsTickTimes.size() > TICK_TIME_QUEUE) {
                    latestPhysicsTickTimes.remove();
                }
            } else {
                // If physics are disabled then sleep for 100 ms.
                // If we don't sleep then we waste a ton of CPU just being in this while(true) loop.
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // If we get to this point of run(), then we are about to return and this thread
        // will terminate soon.
        log.trace(name + " killed");
    }

    private long lastPacketSendTime = 0;

    private void physicsTick(double delta) {
        // Update the immutable ship list.
        immutableShipsList = ((IHasShipManager) hostWorld).getManager().getAllLoadedThreadSafe();

        // Run tasks queued to run on physics thread
        recurringTasks.forEach(t -> t.runTask(delta));
        taskQueue.forEach(Runnable::run);
        taskQueue.clear();

        // Make a sublist of physics objects to process physics on.
        List<PhysicsObject> physicsEntitiesToDoPhysics = new ArrayList<>();
        for (PhysicsObject physicsObject : immutableShipsList) {
            if (physicsObject.isPhysicsEnabled() && physicsObject.getCachedSurroundingChunks() != null) {
                physicsEntitiesToDoPhysics.add(physicsObject);
            }
        }

        // Finally, actually process the physics tick
        tickThePhysicsAndCollision(physicsEntitiesToDoPhysics, delta);

        // Send ship position update packets around 20 times a second
        final long currentTimeMillis = System.currentTimeMillis();
        final double secondsSinceLastPacket = (currentTimeMillis - lastPacketSendTime) / 1000.0;

        // Use .04 to guarantee we're always sending at least 20 packets per second
        if (secondsSinceLastPacket > .04) {
            // Update the last update time
            lastPacketSendTime = currentTimeMillis;

            try {
                // At the end, send the transform update packets
                final ShipTransformUpdateMessage shipTransformUpdateMessage = new ShipTransformUpdateMessage();
                final int dimensionID = hostWorld.provider.getDimension();

                shipTransformUpdateMessage.setDimensionID(dimensionID);
                for (final PhysicsObject physicsObject : immutableShipsList) {
                    final UUID shipUUID = physicsObject.getUuid();
                    final ShipTransform shipTransform = physicsObject.getShipTransformationManager().getCurrentPhysicsTransform();
                    final AxisAlignedBB shipBB = physicsObject.getPhysicsTransformAABB();

                    shipTransformUpdateMessage.addData(shipUUID, shipTransform, shipBB);
                }
                ValkyrienSkiesMod.physWrapperTransformUpdateNetwork.sendToDimension(shipTransformUpdateMessage, shipTransformUpdateMessage.getDimensionID());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Ticks physics and collision for the List of PhysicsWrapperEntity passed in.
     */
    private void tickThePhysicsAndCollision(List<PhysicsObject> shipsWithPhysics, double timeStep) {
        final List<ShipCollisionTask> collisionTasks = new ArrayList<>(
            shipsWithPhysics.size() * 2);
        final List<WaterForcesTask> waterForcesTasks = new ArrayList<>();
        for (PhysicsObject wrapper : shipsWithPhysics) {
            // Update the physics simulation
            try {
                wrapper.getPhysicsCalculations().rawPhysTickPreCol(timeStep);
                // Do water collision and buoyancy
                wrapper.getPhysicsCalculations().getWorldWaterCollider().tickUpdatingTheCollisionCache();
                // Add water forces tasks to be processed in parallel
                waterForcesTasks.addAll(wrapper.getPhysicsCalculations().getWorldWaterCollider().generateWaterForceTasks());
                // Update the collision task if necessary
                wrapper.getPhysicsCalculations().getWorldCollision()
                        .tickUpdatingTheCollisionCache();
                // Take the big collision and split into tiny ones
                wrapper.getPhysicsCalculations().getWorldCollision()
                        .splitIntoCollisionTasks(collisionTasks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final List<Callable<Void>> allTasks = new ArrayList<>();
        allTasks.addAll(collisionTasks);
        allTasks.addAll(waterForcesTasks);

        try {
            // Run all the block collision and water physics tasks
            ValkyrienSkiesMod.getPhysicsThreadPool().invokeAll(allTasks);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Handle the results of water force tasks
        for (final WaterForcesTask waterForcesTask : waterForcesTasks) {
            waterForcesTask.addForcesToShip();
        }

        // Then those collision points have to be processed sequentially afterwards, all in
        // this thread. Thankfully this step is not cpu intensive.
        for (ShipCollisionTask task : collisionTasks) {
            task.getToTask().processCollisionTask(task);
        }

        for (PhysicsObject wrapper : shipsWithPhysics) {
            try {
                wrapper.getPhysicsCalculations().rawPhysTickPostCol();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Marks this physics thread for death. Doesn't immediately end the thread, but instead ensures
     * the thread will die after the current running physics tick is finished.
     */
    public void kill() {
        log.trace(name + " marked for death.");
        threadRunning = false;
    }

    /**
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
        return getNsPerTick();
    }
}
