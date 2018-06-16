/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.nodenetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.physics.management.PhysicsObject;

public class VWNode_TileEntity implements IVWNode {

	private final TileEntity parentTile;
	// No duplicate connections, use Set<Node> to guarantee this
	private final Set<BlockPos> linkedNodesPos;
	private final Set<BlockPos> unmodifiableLinkedNodesPos;
	private boolean isValid;
	private boolean isRelay;

	public VWNode_TileEntity(TileEntity parent) {
		this.parentTile = parent;
		this.linkedNodesPos = new HashSet<BlockPos>();
		this.unmodifiableLinkedNodesPos = Collections.unmodifiableSet(linkedNodesPos);
		this.isValid = false;
		this.isRelay = false;
	}

	@Override
	public Iterable<IVWNode> getConnectedNodes() {
		List<IVWNode> nodesList = new ArrayList<IVWNode>();
		for (BlockPos pos : linkedNodesPos) {
			IVWNode node = getVWNode_TileEntity(getNodeWorld(), pos);
			if (node != null) {
				nodesList.add(node);
			}
		}
		return nodesList;
	}

	@Override
	public void makeConnection(IVWNode other) {
		getLinkedNodesPosMutable().add(other.getNodePos());
		other.getLinkedNodesPosMutable().add(this.getNodePos());

		sendNodeUpdates();
		other.sendNodeUpdates();
	}

	@Override
	public void breakConnection(IVWNode other) {
		getLinkedNodesPosMutable().remove(other.getNodePos());
		other.getLinkedNodesPosMutable().remove(this.getNodePos());

		sendNodeUpdates();
		other.sendNodeUpdates();
	}

	@Override
	public BlockPos getNodePos() {
		return parentTile.getPos();
	}

	@Override
	public void validate() {
		isValid = true;
	}

	@Override
	public void invalidate() {
		isValid = false;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Nullable
	private static IVWNode getVWNode_TileEntity(World world, BlockPos pos) {
		if (world == null || pos == null) {
			throw new IllegalArgumentException("Null aruements");
		}
		boolean isChunkLoaded = world.isBlockLoaded(pos);
		if (!isChunkLoaded) {
			return null;
			// throw new IllegalStateException("VWNode_TileEntity wasn't loaded in the
			// world!");
		}
		TileEntity entity = world.getTileEntity(pos);
		if (entity == null) {
			return null;
			// throw new IllegalStateException("VWNode_TileEntity was null");
		}
		if (entity instanceof IVWNodeProvider) {
			IVWNode vwNode = ((IVWNodeProvider) entity).getNode();
			if (!vwNode.isValid()) {
				return null;
				// throw new IllegalStateException("IVWNode was not valid!");
			} else {
				return vwNode;
			}
		} else {
			return null;
			// throw new IllegalStateException("VWNode_TileEntity of different class");
		}
	}

	@Override
	public World getNodeWorld() {
		return parentTile.getWorld();
	}

	@Override
	public Set<BlockPos> getImmutableLinkedNodesPos() {
		return unmodifiableLinkedNodesPos;
	}

	@Override
	public Set<BlockPos> getLinkedNodesPosMutable() {
		return linkedNodesPos;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		int[] positions = new int[getImmutableLinkedNodesPos().size() * 3];
		int cont = 0;
		for (BlockPos pos : getImmutableLinkedNodesPos()) {
			positions[cont] = pos.getX();
			positions[cont + 1] = pos.getY();
			positions[cont + 2] = pos.getZ();
			cont += 3;
		}
		compound.setIntArray("VWNode_Tile_Data", positions);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		int[] positions = compound.getIntArray("VWNode_Tile_Data");
		for (int i = 0; i < positions.length; i += 3) {
			getLinkedNodesPosMutable().add(new BlockPos(positions[i], positions[i + 1], positions[i + 2]));
		}
	}

	@Override
	public void setIsNodeRelay(boolean isRelay) {
		this.isRelay = isRelay;
	}

	@Override
	public boolean isNodeRelay() {
		return isRelay;
	}

	public PhysicsObject getPhysicsObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendNodeUpdates() {
		if (!this.getNodeWorld().isRemote) {
			Packet toSend = parentTile.getUpdatePacket();

			double xPos = parentTile.getPos().getX();
			double yPos = parentTile.getPos().getY();
			double zPos = parentTile.getPos().getZ();

			WorldServer serverWorld = (WorldServer) this.getNodeWorld();
			PlayerList list = serverWorld.mcServer.getPlayerList();
			// System.out.println("help");
			if (!parentTile.isInvalid()) {
				list.sendToAllNearExcept(null, xPos, yPos, zPos, 128D, serverWorld.provider.getDimension(), toSend);
			}
		}
	}

}
