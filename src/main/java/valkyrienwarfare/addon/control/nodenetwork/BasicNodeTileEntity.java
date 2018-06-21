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
import java.util.Iterator;

import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;
import gigaherz.graph.api.Mergeable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public abstract class BasicNodeTileEntity extends TileEntity implements IVWNodeProvider, ITickable {

	private final VWNode_TileEntity tileNode;
	private boolean firstUpdate;

	public BasicNodeTileEntity() {
		this.tileNode = new VWNode_TileEntity(this);
		this.firstUpdate = true;
		Graph.integrate(tileNode, Collections.EMPTY_LIST, (graph) -> new GraphData());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		tileNode.readFromNBT(compound);
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		return writeToNBT(toReturn);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		tileNode.writeToNBT(compound);
		return super.writeToNBT(compound);
	}

	@Override
	public VWNode_TileEntity getNode() {
		return tileNode;
	}

	@Override
	public void invalidate() {
		// The Node just got destroyed
		this.tileEntityInvalid = true;
		VWNode_TileEntity toInvalidate = getNode();
		toInvalidate.breakAllConnections();
		toInvalidate.invalidate();
		Graph graph = toInvalidate.getGraph();
		if (graph != null) {
			graph.remove(toInvalidate);
		}
	}

	/**
	 * validates a tile entity
	 */
	@Override
	public void validate() {
		this.tileEntityInvalid = false;
		getNode().validate();
	}

	@Override
	public void update() {
		if (firstUpdate) {
			firstUpdate = false;
			init();
		}
	}

	private void init() {
		tileNode.getGraph().addNeighours(tileNode, tileNode.getNeighbours());
	}

	public static class GraphData implements Mergeable<GraphData> {
		private static int sUid = 0;

		private final int uid;

		public GraphData() {
			uid = ++sUid;
		}

		public GraphData(int uid) {
			this.uid = uid;
		}

		@Override
		public GraphData mergeWith(GraphData other) {
			return new GraphData(uid + other.uid);
		}

		@Override
		public GraphData copy() {
			return new GraphData();
		}

		public int getUid() {
			return uid;
		}
	}

	@Override
	public Iterable<IVWNode> getNetworkedConnections() {
		Iterator<GraphObject> objects = tileNode.getGraph().getObjects().iterator();
		Iterator<IVWNode> nodes = new IteratorCaster(objects);
		return new Iterable<IVWNode>() {
			@Override
			public Iterator<IVWNode> iterator() {
				return nodes;
			}
		};
	}

	private class IteratorCaster implements Iterator<IVWNode> {
		private final Iterator toCast;

		private IteratorCaster(Iterator toCast) {
			this.toCast = toCast;
		}

		@Override
		public boolean hasNext() {
			return toCast.hasNext();
		}

		@Override
		public IVWNode next() {
			return (IVWNode) toCast.next();
		}
	}

}
