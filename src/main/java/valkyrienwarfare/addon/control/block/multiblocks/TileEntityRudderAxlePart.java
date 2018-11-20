package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.collision.polygons.Polygon;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityRudderAxlePart extends TileEntityMultiblockPartForce {

	// Angle must be between -90 and 90
	private double rudderAngle;
	// For client rendering purposes only
	private double prevRudderAngle;
	private double nextRudderAngle;
	
	public TileEntityRudderAxlePart() {
		super();
		this.rudderAngle = 0;
		this.prevRudderAngle = 0;
		this.nextRudderAngle = 0;
	}
	
	@Override
	public void update() {
		super.update();
		this.prevRudderAngle = rudderAngle;
		if (this.getWorld().isRemote) {
			// Do this to smooth out lag between the server sending packets.
			this.rudderAngle = rudderAngle + .5 * (nextRudderAngle - rudderAngle);
		}
	}

	public void setRudderAngle(double forcedValue) {
		this.rudderAngle = forcedValue;
		SPacketUpdateTileEntity spacketupdatetileentity = getUpdatePacket();
		WorldServer serverWorld = (WorldServer) world;
		serverWorld.mcServer.getPlayerList().sendToAllNearExcept(null, this.getPos().getX(), getPos().getY(),
				getPos().getZ(), 128D, getWorld().provider.getDimension(), spacketupdatetileentity);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		rudderAngle = compound.getDouble("rudderAngle");
	}

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        toReturn.setDouble("rudderAngle", rudderAngle);
        return toReturn;
    }
	
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
      	double currentRudderAngle = rudderAngle;
    	super.onDataPacket(net, pkt);
    	nextRudderAngle = pkt.getNbtCompound().getDouble("rudderAngle");
    	this.rudderAngle = currentRudderAngle;
    }
    
	@Override
	public VectorImmutable getForceOutputNormal() {
		// TODO Auto-generated method stub
		return new VectorImmutable(1, 0, 0);
	}

	@Override
	public double getThrustMagnitude() {
		return 0;
	}
	
	public Optional<EnumFacing> getRudderAxleAxisDirection() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleAxisDirection());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<EnumFacing> getRudderAxleFacingDirection() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleFacingDirection());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<Integer> getRudderAxleLength() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleLength());
		} else {
			return Optional.empty();
		}
	}

	private Optional<RudderAxleMultiblockSchematic> getRudderAxleSchematic() {
		IMulitblockSchematic schematic = getMultiblockSchematic();
		if (this.isPartOfAssembledMultiblock() && schematic instanceof RudderAxleMultiblockSchematic) {
			return Optional.of((RudderAxleMultiblockSchematic) schematic);
		} else {
			return Optional.empty();
		}
	}
	
	public double getRudderAngle() {
		return this.rudderAngle;
	}
	
	public double getRenderRudderAngle(double partialTicks) {
		return this.prevRudderAngle + ((this.rudderAngle - this.prevRudderAngle) * partialTicks);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	if (this.isPartOfAssembledMultiblock() && this.isMaster() && getRudderAxleAxisDirection().isPresent()) {
    		BlockPos minPos = this.pos;
    		EnumFacing axleAxis = getRudderAxleAxisDirection().get();
			EnumFacing axleFacing = getRudderAxleFacingDirection().get();
			Vec3i otherAxis = axleAxis.getDirectionVec().crossProduct(axleFacing.getDirectionVec());
			
			int nexAxisX = axleAxis.getDirectionVec().getX() + axleFacing.getDirectionVec().getX();
			int nexAxisY = axleAxis.getDirectionVec().getY() + axleFacing.getDirectionVec().getY();
			int nexAxisZ = axleAxis.getDirectionVec().getZ() + axleFacing.getDirectionVec().getZ();
			
			int axleLength = getRudderAxleLength().get();
			
			int offsetX = nexAxisX * axleLength;
			int offsetY = nexAxisY * axleLength;
			int offsetZ = nexAxisZ * axleLength;
			
    		BlockPos maxPos = minPos.add(offsetX, offsetY, offsetZ);
    		
    		int otherAxisXExpanded = otherAxis.getX() * axleLength;
    		int otherAxisYExpanded = otherAxis.getY() * axleLength;
    		int otherAxisZExpanded = otherAxis.getZ() * axleLength;
    		
    		AxisAlignedBB toReturn = (new AxisAlignedBB(minPos, maxPos)).grow(otherAxisXExpanded, otherAxisYExpanded, otherAxisZExpanded).grow(.5, .5, .5);
    		
    		// Do this to transform the output when in ship space.
    		PhysicsWrapperEntity physicsEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
    		if (physicsEntity != null) {
    			Polygon polygon = new Polygon(toReturn, physicsEntity.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);
    			toReturn = polygon.getEnclosedAABB();
    		}
    		return toReturn;
    	} else {
			return super.getRenderBoundingBox();
    	}
    }
	
}
