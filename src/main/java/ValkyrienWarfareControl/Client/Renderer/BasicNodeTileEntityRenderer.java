package ValkyrienWarfareControl.Client.Renderer;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareControl.ThrustNetwork.BasicNodeTileEntity;
import ValkyrienWarfareControl.ThrustNetwork.Node;
import net.minecraft.client.model.ModelLeashKnot;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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

				GL11.glPushMatrix();

				GlStateManager.resetColor();;

			    GlStateManager.enableAlpha();
			    bindTexture(new ResourceLocation("textures/entity/lead_knot.png"));

			    GL11.glTranslated(x + .5D, y + .5D, z +.5D);
//			    GlStateManager.scale(-1.0F, -1.0F, 1.0F);

			    ModelLeashKnot knotRenderer = new ModelLeashKnot();



			    knotRenderer.knotRenderer.render(0.0625F);




				BlockPos originPos = te.getPos();

//				double x = originPos.getX() + .5D;

				for(BlockPos otherPos : tileNode.connectedNodesBlockPos){
					//Render wire between these two blockPos

					GL11.glPushMatrix();

					double otherX = otherPos.getX() - TileEntityRendererDispatcher.staticPlayerX - x;
					double otherY = otherPos.getY() - TileEntityRendererDispatcher.staticPlayerY - y;
					double otherZ = otherPos.getZ() - TileEntityRendererDispatcher.staticPlayerZ - z;


//					System.out.println(otherZ);
//					GL11.glTranslated(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());

					GL11.glLineWidth(2.5f);
					GL11.glColor3d(1.0, 0.0, 0.0);
					GL11.glBegin(GL11.GL_LINES);
					GL11.glVertex3d(0, 0, 0D);
					GL11.glVertex3d(otherX, otherY, otherZ);
					GL11.glEnd();

					GL11.glPopMatrix();

				}
				GL11.glPopMatrix();
			}
		}
	}

}
