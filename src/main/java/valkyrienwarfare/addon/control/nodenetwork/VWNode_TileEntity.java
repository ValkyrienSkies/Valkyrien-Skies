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
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class VWNode_TileEntity implements IVWNode {

	private final TileEntity parentTile;
	// No duplicate connections, use Set<Node> to guarantee this
	private final Set<BlockPos> linkedNodesPos;
	// A wrapper unmodifiable Set that allows external classes to see an immutable
	// version of linkedNodesPos.
	private final Set<BlockPos> unmodifiableLinkedNodesPos;
	private boolean isValid;
	private boolean isRelay;
	private PhysicsObject parentPhysicsObject;

	public VWNode_TileEntity(TileEntity parent) {
		this.parentTile = parent;
		this.linkedNodesPos = new HashSet<BlockPos>();
		this.unmodifiableLinkedNodesPos = Collections.unmodifiableSet(linkedNodesPos);
		this.isRelay = false;
		this.isValid = false;
	}

	@Override
	public Iterable<IVWNode> getDirectlyConnectedNodes() {
		assertValidity();
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
		assertValidity();
		boolean contains = linkedNodesPos.contains(other.getNodePos());
		if (!contains) {
			linkedNodesPos.add(other.getNodePos());
			parentTile.markDirty();
			other.makeConnection(this);
			sendNodeUpdates();
		}
	}

	@Override
	public void breakConnection(IVWNode other) {
		assertValidity();
		boolean contains = linkedNodesPos.contains(other.getNodePos());
		if (contains) {
			linkedNodesPos.remove(other.getNodePos());
			parentTile.markDirty();
			other.breakConnection(this);
			sendNodeUpdates();
		}
	}

	@Override
	public BlockPos getNodePos() {
		assertValidity();
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
			throw new IllegalArgumentException("Null arguments");
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
	public Set<BlockPos> getLinkedNodesPos() {
		return unmodifiableLinkedNodesPos;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		int[] positions = new int[getLinkedNodesPos().size() * 3];
		int cont = 0;
		for (BlockPos pos : getLinkedNodesPos()) {
			positions[cont] = pos.getX();
			positions[cont + 1] = pos.getY();
			positions[cont + 2] = pos.getZ();
			cont += 3;
		}
		compound.setIntArray(NBT_DATA_KEY, positions);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		int[] positions = compound.getIntArray(NBT_DATA_KEY);
		for (int i = 0; i < positions.length; i += 3) {
			linkedNodesPos.add(new BlockPos(positions[i], positions[i + 1], positions[i + 2]));
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

	@Override
	public PhysicsObject getPhysicsObject() {
		return parentPhysicsObject;
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

	private void assertValidity() {
		if (!isValid()) {
			throw new IllegalStateException("This node is not valid / initialized!");
		}
	}

	@Override
	public void shiftConnections(BlockPos offset) {
		if (isValid()) {
			throw new IllegalStateException("Cannot shift the connections of a Node while it is valid and in use!");
		}
		List<BlockPos> shiftedNodesPos = new ArrayList<BlockPos>(linkedNodesPos.size());
		for (BlockPos originalPos : linkedNodesPos) {
			shiftedNodesPos.add(originalPos.add(offset));
		}
		linkedNodesPos.clear();
		linkedNodesPos.addAll(shiftedNodesPos);
	}

	@Override
	public void setParentPhysicsObject(PhysicsObject parent) {
		if (isValid()) {
			throw new IllegalStateException(
					"Cannot change the parent physics object of a Node while it is valid and in use!");
		}
		this.parentPhysicsObject = parent;
	}

}
