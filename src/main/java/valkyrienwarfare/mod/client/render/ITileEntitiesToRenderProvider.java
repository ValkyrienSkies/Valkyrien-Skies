package valkyrienwarfare.mod.client.render;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;

public interface ITileEntitiesToRenderProvider {

	@Nullable
	List<TileEntity> getTileEntitiesToRender(int chunkExtendedDataIndex);
}
