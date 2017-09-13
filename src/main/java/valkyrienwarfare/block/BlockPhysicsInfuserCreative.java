package valkyrienwarfare.block;

import valkyrienwarfare.relocation.DetectorManager;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class BlockPhysicsInfuserCreative extends BlockPhysicsInfuser {
	
	public BlockPhysicsInfuserCreative(Material materialIn) {
		super(materialIn);
		shipSpawnDetectorID = DetectorManager.DetectorIDs.BlockPosFinder.ordinal();
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Turns any blocks attatched to this one into a brand new Ship, just be careful not to infuse your entire world");
		itemInformation.add(TextFormatting.RED + "" + TextFormatting.RED + TextFormatting.ITALIC + "Warning, the creative infuser has no block limits, and it will infuse everything it touches! Use with extreme caution.");
	}
	
}