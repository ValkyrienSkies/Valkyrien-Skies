package org.valkyrienskies.mod.common.multithreaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorldProvider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.physics.collision.optimization.ShipCollisionTask;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;

/**
 * Handles all the physics processing for a world separate from the game tick.
 *
 * @author thebest108
 */
@Log4j2
public class VSThread extends Thread {

    private final static long NS_PER_TICK = 10000000;
    private final static long MAX_LOST_TIME_NS = 1000000000;
    // The number of physics ticks to be considered in the average tick time.
    private final static long TICK_TIME_QUEUE = 100;
    // Used to give each VS thread a unique name
    private static int threadID = 0;
    private final World hostWorld;
    private final Queue<Long> latestPhysicsTickTimes;
    // The ships we will be ticking physics for every tick, and sending those
    // updates to players.
    private int physicsTicksCount;
    // Used by the game thread to mark this thread for death.
    private volatile boolean threadRunning;

    private Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();

    public VSThread(World host) {
        super("VS World Thread " + threadID);
        threadID++;
        this.hostWorld = host;
        this.physicsTicksCount = 0;
        this.threadRunning = true;
        this.latestPhysicsTickTimes = new ConcurrentLinkedQueue<>();
        log.trace(this.getName() + " thread created.");
    }

    @SideOnly(Side.CLIENT)
    private static boolean isSinglePlayerPaused() {
        return Minecraft.getMinecraft().isGamePaused();
    }

    public void addScheduledTask(Runnable r) {
        taskQueue.add(r);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
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
        // will terminate soon.
        log.trace(super.getName() + " killed");
    }

    private void runGameLoop() {
        // Run tasks queued to run on physics thread
        taskQueue.forEach(Runnable::run);
        taskQueue.clear();

        MinecraftServer mcServer = hostWorld.getMinecraftServer();
        assert mcServer != null;
        if (mcServer.isServerRunning()) {
            if (mcServer.isDedicatedServer()) {
                // Always tick the physics
                physicsTick();
            } else {
                // Only tick the physics if the game isn't paused
                if (!isSinglePlayerPaused()) {
                    physicsTick();
                }
            }
        }
    }

    // The whole time need to be careful the game thread isn't messing with these
    // values.
    private void physicsTick() {
        // TODO: Temporary fix:
//        WorldPhysObjectManager manager = ValkyrienSkiesMod.VS_PHYSICS_MANAGER
//            .getManagerForWorld(hostWorld);
        List<PhysicsObject> physicsEntities = ((IHasShipManager) hostWorld).getManager().getAllLoadedPhysObj();
        // Tick ship physics here
        tickThePhysicsAndCollision(physicsEntities);
        tickSendUpdatesToPlayers(physicsEntities);
    }

    /**
     * Ticks physics and collision for the List of PhysicsWrapperEntity passed in.
     */
    private void tickThePhysicsAndCollision(List<PhysicsObject> shipsWithPhysics) {
        double newPhysSpeed = VSConfig.physSpeed;
        List<ShipCollisionTask> collisionTasks = new ArrayList<>(
            shipsWithPhysics.size() * 2);
        for (PhysicsObject wrapper : shipsWithPhysics) {
//            if (!wrapper.firstUpdate) {
                // Update the physics simulation
            try {
                wrapper.getPhysicsCalculations().rawPhysTickPreCol(newPhysSpeed);
                // Update the collision task if necessary
                wrapper.getPhysicsCalculations().getWorldCollision()
                        .tickUpdatingTheCollisionCache();
                // Take the big collision and split into tiny ones
                wrapper.getPhysicsCalculations().getWorldCollision()
                        .splitIntoCollisionTasks(collisionTasks);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }

        // Process gear physics simulation for the game worlds.
        IRotationNodeWorldProvider rotationNodeWorldProvider = (IRotationNodeWorldProvider) hostWorld;
        rotationNodeWorldProvider.getPhysicsRotationNodeWorld().processTorquePhysics(newPhysSpeed);

        try {
            // The individual collision tasks will sort through a lot of data to find
            // collision points
            ValkyrienSkiesMod.getPHYSICS_THREADS_EXECUTOR().invokeAll(collisionTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Then those collision points have to be processed sequentially afterwards, all in
        // this thread. Thankfully this step is not cpu intensive.
        for (ShipCollisionTask task : collisionTasks) {
            // PhysicsWrapperEntity wrapper = task.getToTask().getParent().getWrapperEntity();
//            if (!wrapper.firstUpdate) {
                task.getToTask().processCollisionTask(task);
//            }
        }

        for (PhysicsObject wrapper : shipsWithPhysics) {
//            if (!wrapper.firstUpdate) {
                try {
                    wrapper.getPhysicsCalculations().rawPhysTickPostCol();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            } else {
//                wrapper.getPhysicsObject()
//                    .getShipTransformationManager()
//                        .updateAllTransforms(false, false);
//            }
        }
    }

    private void tickSendUpdatesToPlayers(List<PhysicsObject> ships) {
//        for (PhysicsWrapperEntity wrapper : ships) {
//            wrapper.getPhysicsObject().getShipTransformationManager()
//                .sendPositionToPlayers(physicsTicksCount);
//        }
        physicsTicksCount++;
    }

    /**
     * Marks this physics thread for death. Doesn't immediately end the thread, but instead ensures
     * the thread will die after the current running physics tick is finished.
     */
    public void kill() {
        log.trace(super.getName() + " marked for death.");
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
        return NS_PER_TICK;
    }
}
