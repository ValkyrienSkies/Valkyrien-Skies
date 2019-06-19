package valkyrienwarfare.physics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class PhysicsParticleManager {

    private final PhysicsCalculations parent;
    private List<PhysicsParticle> physicsParticles;

    public PhysicsParticleManager(PhysicsCalculations parent) {
        this.parent = parent;
        this.physicsParticles = new ArrayList<PhysicsParticle>();
    }

    public void spawnPhysicsParticle(float posX, float posY, float posZ, float velX, float velY, float velZ, float mass, float timeToLive) {
        physicsParticles.add(new PhysicsParticle(posX, posY, posZ, velX, velY, velZ, mass, timeToLive));
    }

    // This only gets called after the particle spawning step is finished.
    public void physicsTickAfterAllPreForces(float timeStep) {
        List<PhysicsParticle> aliveParticles = new ArrayList<PhysicsParticle>(physicsParticles.size());
        MutableBlockPos bufferBlockPos = new MutableBlockPos();
        Vector bufferVector = new Vector();
        Vector bufferVectorForcePos = new Vector();
        Vector bufferVectorForce = new Vector();
        Vector bufferVectorCrossProduct = new Vector();
        for (PhysicsParticle physicsParticle : physicsParticles) {
            physicsParticle.tickParticle(parent, bufferBlockPos, bufferVector, timeStep);
            if (!physicsParticle.isParticleDead()) {
                aliveParticles.add(physicsParticle);
            } else {
                if (physicsParticle.addMomentumToShip) {
                    bufferVectorForcePos.setValue(physicsParticle.posX - parent.getParent().getWrapperEntity().posX,
                            physicsParticle.posY - parent.getParent().getWrapperEntity().posY,
                            physicsParticle.posZ - parent.getParent().getWrapperEntity().posZ);
                    bufferVectorForce.setValue(physicsParticle.velX * physicsParticle.mass,
                            physicsParticle.velY * physicsParticle.mass, physicsParticle.velZ * physicsParticle.mass);
                    parent.addForceAtPoint(bufferVectorForcePos, bufferVectorForce);
                }
            }
        }
        this.physicsParticles = aliveParticles;
    }

    private class PhysicsParticle {

        private float posX;
        private float posY;
        private float posZ;
        private float velX;
        private float velY;
        private float velZ;
        private float mass;
        // How many seconds this particle has to 'live'.
        private float particleLife;
        private boolean isParticleDead;
        private boolean addMomentumToShip;

        public PhysicsParticle(float posX, float posY, float posZ, float velX, float velY, float velZ, float mass, float particleLife) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.velX = velX;
            this.velY = velY;
            this.velZ = velZ;
            this.mass = mass;
            this.particleLife = particleLife;
            this.isParticleDead = false;
        }

        public Vector getParticleMomentum() {
            return new Vector((double) (velX * mass), (double) (velY * mass), (double) (velZ * mass));
        }

        public void tickParticle(PhysicsCalculations physicsSource, MutableBlockPos bufferBlockPos, Vector bufferVector, float timeStep) {
            // First move the particle forward
            this.posX += velX * timeStep;
            this.posY += velY * timeStep;
            this.posZ += velZ * timeStep;
            // Then advance the particle death clock
            this.particleLife -= timeStep;
            this.isParticleDead = (particleLife < 0);
            // Then check for collision in the world
            bufferBlockPos.setPos(posX, posY, posZ);
            if (!canParticlePassThrough(physicsSource.getParent().getWorldObj(), bufferBlockPos)) {
                // The particle hit a block in the world, so kill it.
                this.isParticleDead = true;
            }
            // If the particle still isn't dead then check for collision in ship
            if (!isParticleDead) {
                bufferVector.setValue(posX, posY, posZ);
                physicsSource.getParent().getShipTransformationManager().getCurrentPhysicsTransform().transform(bufferVector, TransformType.GLOBAL_TO_SUBSPACE);
                bufferBlockPos.setPos(bufferVector.X, bufferVector.Y, bufferVector.Z);
                if (!canParticlePassThrough(physicsSource.getParent().getWorldObj(), bufferBlockPos)) {
                    this.isParticleDead = true;
                    addMomentumToShip = true;
                }
            }
        }

        // Returns true if the particle can pass through this block
        private boolean canParticlePassThrough(World world, BlockPos pos) {
            ChunkProviderServer serverProvider = (ChunkProviderServer) world.getChunkProvider();
            // If the chunk isn't loaded, then no problem ignore it.
            long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
            if (!serverProvider.loadedChunks.containsKey(chunkKey)) {
                this.isParticleDead = true;
                return true;
            }
            IBlockState collisionState = serverProvider.loadedChunks.get(chunkKey)
                    .getBlockState(pos);
            // Otherwise we have to bypass the world blockstate get because sponge has some protection on it.
            // System.out.println("oof");
            return collisionState.getBlock().isPassable(world, pos);
        }

        public boolean isParticleDead() {
            return isParticleDead;
        }

        public boolean addMomentumToShip() {
            return addMomentumToShip;
        }
    }
}
