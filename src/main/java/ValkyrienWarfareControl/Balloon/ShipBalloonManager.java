package ValkyrienWarfareControl.Balloon;

import java.util.ArrayList;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class ShipBalloonManager {

	public ArrayList<BalloonProcessor> balloonProcessors = new ArrayList<BalloonProcessor>();
	public PhysicsObject parent;
	
	public ShipBalloonManager(PhysicsObject parent){
		this.parent = parent;
	}
	
	//Searches 5 blocks up for a processor, if one cant be found then it returns null
	public BalloonProcessor getProcessorAbovePos(BlockPos burnerPos){
		for(int i=1;i<=5;i++){
			BlockPos toCheck = burnerPos.up(i);
			IBlockState state = parent.VKChunkCache.getBlockState(toCheck);
			Block block = state.getBlock();
			if(block.blockMaterial.blocksMovement()){
				//End the loop
				i = 420;
			}else{
				for(BalloonProcessor processor:balloonProcessors){
					if(processor.isBlockPosInRange(toCheck)){
						if(processor.internalAirPositons.contains(toCheck)){
							return processor;
						}
					}
				}
			}
		}
		return null;
	}
	
	public void addBalloonProcessor(BalloonProcessor toAdd){
		balloonProcessors.add(toAdd);
	}
	
}
