package ValkyrienWarfareControl.Client.Renderer;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareControl.ThrustNetwork.Node;
import ValkyrienWarfareControl.TileEntity.BasicNodeTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
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

//				double x = originPos.getX() + .5D;

				for(BlockPos otherPos : tileNode.connectedNodesBlockPos){
					//Render wire between these two blockPos

					GL11.glPushMatrix();

					double otherX = otherPos.getX() + .5D - TileEntityRendererDispatcher.staticPlayerX;
					double otherY = otherPos.getY() + .5D - TileEntityRendererDispatcher.staticPlayerY;
					double otherZ = otherPos.getZ() + .5D - TileEntityRendererDispatcher.staticPlayerZ;

//					GL11.glTranslated(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());

					GL11.glLineWidth(2.5f);
					GL11.glColor3d(1.0, 0.0, 0.0);
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex3d(x + 0.5D, y + 0.5D, z + 0.5D);
					GL11.glVertex3d(otherX, otherY, otherZ);
					GL11.glEnd();

					GL11.glPopMatrix();

				}

			}
		}
	}

}
