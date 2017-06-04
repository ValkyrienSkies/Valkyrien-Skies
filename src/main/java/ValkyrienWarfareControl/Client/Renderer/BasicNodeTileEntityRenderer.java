package ValkyrienWarfareControl.Client.Renderer;

import ValkyrienWarfareControl.GraphTheory.Node;
import ValkyrienWarfareControl.TileEntity.BasicNodeTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BasicNodeTileEntityRenderer extends TileEntitySpecialRenderer {

	private final Class renderedTileEntityClass;

	public BasicNodeTileEntityRenderer(Class toRender){
		renderedTileEntityClass = toRender;
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick, int destroyStage) {
		if(te instanceof BasicNodeTileEntity){
			Node tileNode = ((BasicNodeTileEntity)(te)).getNode();
			if(tileNode != null){
//				System.out.println(tileNode.connectedNodesBlockPos.size());

				BlockPos originPos = te.getPos();

				for(BlockPos otherPos : tileNode.connectedNodesBlockPos){
					//Render wire between these two blockPos
				}

			}
		}
	}

}
