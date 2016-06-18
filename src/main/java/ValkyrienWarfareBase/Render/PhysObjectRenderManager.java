package ValkyrienWarfareBase.Render;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

/**
 * Object owned by each physObject responsible for handling all rendering operations
 * @author thebest108
 *
 */
public class PhysObjectRenderManager {

	public boolean needsSolidUpdate = true;
	public int glCallListSolid = -1;
	public PhysicsObject parent;
	//This pos is used to prevent Z-Buffer Errors D:
	//It's actual value is completely irrelevant as long as it's close to the 
	//Ship's centerBlockPos
	public BlockPos offsetPos;
	
	public PhysObjectRenderManager(PhysicsObject toRender){
		parent = toRender;
	}
	
	public void updateOffsetPos(BlockPos newPos){
		offsetPos = newPos;
	}
	
	public void updateSolidList(){
		if(offsetPos==null){
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldrenderer = tessellator.getBuffer();
		
		worldrenderer.setTranslation(-offsetPos.getX(), -offsetPos.getY(), -offsetPos.getZ());
		
		GLAllocation.deleteDisplayLists(glCallListSolid);
		glCallListSolid = GLAllocation.generateDisplayLists(1);
		GL11.glPushMatrix();
			GL11.glNewList(glCallListSolid, GL11.GL_COMPILE);
		    	GlStateManager.pushMatrix();
		    	worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
			    IBlockState iblockstate;
		        if (Minecraft.isAmbientOcclusionEnabled()) {
		            GlStateManager.shadeModel(GL11.GL_SMOOTH);
		        } else {
		            GlStateManager.shadeModel(GL11.GL_FLAT);
		        }
			    for(BlockPos pos:parent.blockPositions){
			    	iblockstate=parent.worldObj.getBlockState(pos);
		            if(!iblockstate.getBlock().isTranslucent(iblockstate)){
		            	Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(iblockstate, pos, parent.worldObj, worldrenderer);
		            }
		        }
			    tessellator.draw();
			    GlStateManager.popMatrix();
			GL11.glEndList();
		GL11.glPopMatrix();
		worldrenderer.setTranslation(0,0,0);
		needsSolidUpdate = false;
	}
	
	public void markForUpdate(){
		needsSolidUpdate = true;
	}
	
}
