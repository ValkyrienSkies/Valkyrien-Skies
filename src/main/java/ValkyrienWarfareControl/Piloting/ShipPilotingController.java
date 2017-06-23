package ValkyrienWarfareControl.Piloting;

import java.util.UUID;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Physics.PhysicsCalculations_Zepplin;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareBase.PhysicsManagement.ShipType;
import ValkyrienWarfareControl.Block.BlockShipPilotsChair;
import ValkyrienWarfareControl.TileEntity.TileEntityShipHelm;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Used only on the Server ship entity
 *
 * @author thebest108
 */
public class ShipPilotingController {

    public static final UUID nullID = new UUID(0L, 0L);
    public final PhysicsObject controlledShip;
    private EntityPlayerMP shipPilot;
    //Used for world saving/loading purposes
    private UUID mostRecentPilotID;
    private boolean hasChair = false;
    private BlockPos chairPosition = BlockPos.ORIGIN;

    public ShipPilotingController(PhysicsObject toControl) {
        controlledShip = toControl;
    }

    public static double[] getRotationMatrixFromBlockState(IBlockState state, BlockPos chairPosition) {
        double playerChairYaw = 0;
        if (state.getBlock() instanceof BlockShipPilotsChair) {
            playerChairYaw = BlockShipPilotsChair.getChairYaw(state, chairPosition);
        }
        double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(0.0D, 1.0D, 0.0D, Math.toRadians(playerChairYaw));

        return pilotRotationMatrix;
    }

    public EntityPlayerMP getPilotEntity() {
        return shipPilot;
    }

    public void receivePilotControlsMessage(PilotControlsMessage message, EntityPlayerMP whoSentIt) {
        if (shipPilot == whoSentIt) {
            handlePilotControlMessage(message, whoSentIt);
        }
        if (message.inputType == ControllerInputType.ShipHelm) {
            BlockPos pos = message.controlBlockPos;
            TileEntity tile = whoSentIt.world.getTileEntity(pos);
            if (tile instanceof TileEntityShipHelm) {
                TileEntityShipHelm shipHelm = (TileEntityShipHelm) tile;
                if (message.airshipLeft) {
                    shipHelm.wheelRotation -= 10;

//					System.out.println(shipHelm.wheelRotation);
                }
                if (message.airshipRight) {
                    shipHelm.wheelRotation += 10;
//					System.out.println("YES");
                }
//				System.out.println("YES");
            }
        }
    }

    private void handlePilotControlMessage(PilotControlsMessage message, EntityPlayerMP whoSentIt) {
    	if(controlledShip.shipType == ShipType.Zepplin) {
    		PhysicsCalculations_Zepplin zepplinPhysics = (PhysicsCalculations_Zepplin) controlledShip.physicsProcessor;
    		double forwardRate = zepplinPhysics.forwardRate;
    		double yawRate = zepplinPhysics.yawRate;
    		double upRate = zepplinPhysics.upRate;

    		 if (message.airshipForward) {
    			 forwardRate++;
             }
             if (message.airshipBackward) {
            	 forwardRate--;
             }
             if (message.airshipUp) {
            	 upRate++;
             }
             if (message.airshipDown) {
            	 upRate--;
             }
             if (message.airshipRight) {
            	 yawRate++;
             }
             if (message.airshipLeft) {
            	 yawRate--;
             }

             forwardRate = Math.max(Math.min(forwardRate, 20D), -20D);
             upRate = Math.max(Math.min(upRate, 20D), -20D);
             yawRate = Math.max(Math.min(yawRate, 50D), -50D);

//             if(message.airshipStop) {
//            	 forwardRate = upRate = yawRate = 0;
//             }

             zepplinPhysics.forwardRate = forwardRate;
             zepplinPhysics.upRate = upRate;
             zepplinPhysics.yawRate = yawRate;

             return;
    	}

    	//Set to whatever the player was pointing at in Ship space
        //These vectors can be re-arranged depending on the direction the chair was placed

        IBlockState state = controlledShip.worldObj.getBlockState(chairPosition);
//		double[] pilotRotationMatrix = getRotationMatrixFromBlockState(state, chairPosition);

        if (state.getBlock() instanceof BlockShipPilotsChair) {

            double pilotPitch = 0D;
            double pilotYaw = ((BlockShipPilotsChair) state.getBlock()).getChairYaw(state, chairPosition);
            double pilotRoll = 0D;

            double[] pilotRotationMatrix = RotationMatrices.getRotationMatrix(pilotPitch, pilotYaw, pilotRoll);

            Vector playerDirection = new Vector(1, 0, 0);

            Vector rightDirection = new Vector(0, 0, 1);

            Vector leftDirection = new Vector(0, 0, -1);

            RotationMatrices.applyTransform(pilotRotationMatrix, playerDirection);
            RotationMatrices.applyTransform(pilotRotationMatrix, rightDirection);
            RotationMatrices.applyTransform(pilotRotationMatrix, leftDirection);

            Vector upDirection = new Vector(0, 1, 0);

            Vector downDirection = new Vector(0, -1, 0);

            Vector idealAngularDirection = new Vector();

            Vector idealLinearVelocity = new Vector();

            Vector shipUp = new Vector(0, 1, 0);
            Vector shipUpPos = new Vector(0, 1, 0);

            if (message.airshipForward) {
                idealLinearVelocity.add(playerDirection);
            }
            if (message.airshipBackward) {
                idealLinearVelocity.subtract(playerDirection);
            }

            RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealLinearVelocity);

            RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, shipUp);

            if (message.airshipUp) {
                idealLinearVelocity.add(upDirection);
            }
            if (message.airshipDown) {
                idealLinearVelocity.add(downDirection);
            }


            if (message.airshipRight) {
                idealAngularDirection.add(rightDirection);
            }
            if (message.airshipLeft) {
                idealAngularDirection.add(leftDirection);
            }

            //Upside down if you want it
//			Vector shipUpOffset = shipUpPos.getSubtraction(shipUp);
            Vector shipUpOffset = shipUp.getSubtraction(shipUpPos);


            double mass = controlledShip.physicsProcessor.mass;

//			idealAngularDirection.multiply(mass/2.5D);
            idealLinearVelocity.multiply(mass / 5D);
//			shipUpOffset.multiply(mass/2.5D);


            idealAngularDirection.multiply(1D / 6D);
            shipUpOffset.multiply(1D / 3D);

            Vector velocityCompenstationLinear = controlledShip.physicsProcessor.linearMomentum;

            Vector velocityCompensationAngular = controlledShip.physicsProcessor.angularVelocity.cross(playerDirection);

            Vector velocityCompensationAlignment = controlledShip.physicsProcessor.angularVelocity.cross(shipUpPos);

            velocityCompensationAlignment.multiply(controlledShip.physicsProcessor.physRawSpeed);
            velocityCompensationAngular.multiply(2D * controlledShip.physicsProcessor.physRawSpeed);

            shipUpOffset.subtract(velocityCompensationAlignment);
            velocityCompensationAngular.subtract(velocityCompensationAngular);

            RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, idealAngularDirection);
            RotationMatrices.applyTransform3by3(controlledShip.physicsProcessor.framedMOI, shipUpOffset);


            if (message.airshipSprinting) {
                idealLinearVelocity.multiply(2D);
            }

            idealLinearVelocity.subtract(idealAngularDirection);
            idealLinearVelocity.subtract(shipUpOffset);

            //TEMPORARY CODE!!!

            controlledShip.physicsProcessor.addForceAtPoint(playerDirection, idealAngularDirection);

            controlledShip.physicsProcessor.addForceAtPoint(shipUpPos, shipUpOffset);

            controlledShip.physicsProcessor.addForceAtPoint(new Vector(), idealLinearVelocity);

            controlledShip.physicsProcessor.convertTorqueToVelocity();

//			RotationMatrices.applyTransform(controlledShip.coordTransform.lToWRotation, idealAngularDirection);
//			System.out.println(idealAngularDirection);
        }
    }

}
