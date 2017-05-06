package ValkyrienWarfareBase.Block;

import ValkyrienWarfareBase.Relocation.DetectorManager;
import net.minecraft.block.material.Material;

public class BlockPhysicsInfuserCreative extends BlockPhysicsInfuser {

	public BlockPhysicsInfuserCreative(Material materialIn) {
		super(materialIn);
		shipSpawnDetectorID = DetectorManager.DetectorIDs.BlockPosFinder.ordinal();
	}

}
