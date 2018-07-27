package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import valkyrienwarfare.math.Vector;

/**
 * A lot of this class is temporary, but will basically act as a multiblock
 * TileEntity that also creates propulsion force.
 * 
 * @author thebest108
 *
 */
public class TileEntityEtherCompressorPanel extends TileEntityEtherPropulsion {

	private boolean isPartOfMultiBlock;
	private boolean isMultiBlockMaster;
	// This TileEntity will always try to poll information from the TileEntity at
	// the center, and if it fails it will default to doing nothing.
	private BlockPos masterPos;

	/**
	 * This constructor is always called by the empty constructor, so its safe to
	 * initialize all the variables here.
	 * 
	 * @param vector
	 * @param power
	 */
	public TileEntityEtherCompressorPanel(Vector vector, double power) {
		super(vector, power);
		this.isPartOfMultiBlock = false;
		this.isMultiBlockMaster = false;
		this.masterPos = BlockPos.ORIGIN;
	}

	/**
	 * Checks if the multiblock is formed, only returns true if the multiblock
	 * formed correctly and this TileEntity is the master.
	 * 
	 * @return
	 */
	public boolean checkIfMultiBlockFormed() {
		MutableBlockPos testPos = new MutableBlockPos();
		boolean isMasterOfMultiBlock = true;
		for (int xOff = 0; xOff < 2; xOff++) {
			for (int yOff = 0; yOff < 2; yOff++) {
				for (int zOff = 0; zOff < 2; zOff++) {
					testPos.setPos(this.getPos().getX() + xOff, this.getPos().getY() + yOff,
							this.getPos().getZ() + zOff);
					TileEntity tileAtPos = this.getWorld().getTileEntity(testPos);
					if (tileAtPos != null) {
						if (tileAtPos instanceof TileEntityEtherCompressorPanel) {
							TileEntityEtherCompressorPanel tileEtherPanel = (TileEntityEtherCompressorPanel) tileAtPos;
							if (tileEtherPanel.isPartOfMultiBlock()) {
								if (!tileEtherPanel.getMasterPos().equals(this.getPos())) {
									// System.out.println("no");
									isMasterOfMultiBlock = false;
								}
							}
						} else {
							// System.out.println("no");
							isMasterOfMultiBlock = false;
							
						}
					} else {
						// System.out.println("no");
						isMasterOfMultiBlock = false;
					}
				}
			}
		}
		return isMasterOfMultiBlock;
	}
	
	private void setupStructure() {
		MutableBlockPos testPos = new MutableBlockPos();
		// Create the multiblock structure
		for (int xOff = 0; xOff < 2; xOff++) {
			for (int yOff = 0; yOff < 2; yOff++) {
				for (int zOff = 0; zOff < 2; zOff++) {
					testPos.setPos(this.getPos().getX() + xOff, this.getPos().getY() + yOff,
							this.getPos().getZ() + zOff);
					TileEntity tileAtPos = this.getWorld().getTileEntity(testPos);
					TileEntityEtherCompressorPanel tileEtherPanel = (TileEntityEtherCompressorPanel) tileAtPos;
					tileEtherPanel.isPartOfMultiBlock = true;
					tileEtherPanel.masterPos = new BlockPos(testPos);
					if (tileEtherPanel != this) {
						tileEtherPanel.isMultiBlockMaster = false;
					} else {
						this.isMultiBlockMaster = true;
					}
				}
			}
		}
		System.out.println("MADE IT!");
	}

	public void reset() {
	    this.masterPos = BlockPos.ORIGIN;
	    this.isMultiBlockMaster = false;
	    this.isPartOfMultiBlock = false;
	}

	public boolean checkForMaster() {
		TileEntity tile = this.getWorld().getTileEntity(masterPos);
		return tile != null && (tile instanceof TileEntityEtherCompressorPanel)
				&& TileEntityEtherCompressorPanel.class.cast(tile).isMultiBlockMaster();
	}

	/**
	 * Only call this from the master.
	 */
	public void resetStructure() {
		
		if (!this.isMultiBlockMaster()) {
			System.out.println("resetStructure() is only supposed to be called on master TileEntity's!!!");
			return;
		}
		System.out.println("resting structure");
		MutableBlockPos testPos = new MutableBlockPos();
		for (int xOff = 0; xOff < 2; xOff++) {
			for (int yOff = 0; yOff < 2; yOff++) {
				for (int zOff = 0; zOff < 2; zOff++) {
					testPos.setPos(this.getPos().getX() + xOff, this.getPos().getY() + yOff,
							this.getPos().getZ() + zOff);
					TileEntity tileAtPos = this.getWorld().getTileEntity(testPos);
					if (tileAtPos != null && tileAtPos instanceof TileEntityEtherCompressorPanel) {
						TileEntityEtherCompressorPanel tileEtherPanel = (TileEntityEtherCompressorPanel) tileAtPos;
						tileEtherPanel.reset();
					}
				}
			}
		}
	}
	
	@Override
	public void update() {
	    super.update();
	    if (!this.getWorld().isRemote) {
	        if (isPartOfMultiBlock()) { 
	            if (isMultiBlockMaster()) {
	                // Put stuff you want the multiblock to do here!
	            }
	        } else {
	            // Constantly check if structure is formed until it is.
	            if (checkIfMultiBlockFormed())
	                setupStructure();
	        }
	    }
	}
	
	public BlockPos getMasterPos() {
		return masterPos;
	}
	
	public boolean isMultiBlockMaster() {
		return isMultiBlockMaster;
	}
	
	public boolean isPartOfMultiBlock() {
		return isPartOfMultiBlock;
	}
	
	@Override
	public double getThrustActual() {
		return this.getMaxThrust() * this.getCurrentEtherEfficiency(); // * this.getThrustMultiplierGoal();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.isPartOfMultiBlock = compound.getBoolean("isPartOfMultiBlock");
		this.isMultiBlockMaster = compound.getBoolean("isMultiBlockMaster");
		// Only load this from NBT if we are a part of a multi-block.
		if (isPartOfMultiBlock) {
			masterPos = new BlockPos(compound.getInteger("masterX"), compound.getInteger("masterY"),
					compound.getInteger("masterZ"));
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isPartOfMultiBlock", isPartOfMultiBlock);
		compound.setBoolean("isMultiBlockMaster", isMultiBlockMaster);
		compound.setInteger("masterX", masterPos.getX());
		compound.setInteger("masterY", masterPos.getY());
		compound.setInteger("masterZ", masterPos.getZ());
		return super.writeToNBT(compound);
	}
}
