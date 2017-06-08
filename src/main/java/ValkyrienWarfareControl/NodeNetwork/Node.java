package ValkyrienWarfareControl.NodeNetwork;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class Node {

	public final TileEntity parentTile;
	//No duplicate connections
	public HashSet<Node> connectedNodes;
	public HashSet<BlockPos> connectedNodesBlockPos;
	private boolean isFullyBuilt = false;
	private NodeNetwork parentNetwork;

	private byte channel = 0;

	public Node(TileEntity parent){
		parentTile = parent;
		connectedNodes = new HashSet<Node>();
		connectedNodesBlockPos = new HashSet<BlockPos>();
		parentNetwork = new NodeNetwork(this);
	}

	public void linkNode(Node other){
		updateBuildState();
		connectedNodes.add(other);
		other.connectedNodes.add(this);
		connectedNodesBlockPos.add(other.parentTile.getPos());
		other.connectedNodesBlockPos.add(this.parentTile.getPos());
		parentNetwork.mergeWithNetworks(new NodeNetwork[]{other.parentNetwork});

		if(!parentTile.getWorld().isRemote){
			sendUpdatesToNearby();
			other.sendUpdatesToNearby();
		}
	}

	public void unlinkNode(Node other, boolean updateNodeNetwork, boolean sendToClient){
		updateBuildState();
		connectedNodes.remove(other);
		other.connectedNodes.remove(this);
		connectedNodesBlockPos.remove(other.parentTile.getPos());
		other.connectedNodesBlockPos.remove(this.parentTile.getPos());

		if(updateNodeNetwork){
			parentNetwork.recalculateNetworks(this);
		}

		if(sendToClient && !parentTile.getWorld().isRemote){
			sendUpdatesToNearby();
			other.sendUpdatesToNearby();
		}
	}

	public void sendUpdatesToNearby(){
		Packet toSend = parentTile.getUpdatePacket();

		double xPos = parentTile.getPos().getX();
		double yPos = parentTile.getPos().getY();
		double zPos = parentTile.getPos().getZ();

		WorldServer serverWorld = (WorldServer) parentTile.getWorld();
		PlayerList list = serverWorld.mcServer.getPlayerList();
//		System.out.println("help");
		if(!parentTile.isInvalid()){
			list.sendToAllNearExcept(null, xPos, yPos, zPos, 128D, serverWorld.provider.getDimension(), toSend);
		}
	}

	/**
	 * Destroys all other connections to this node from other nodes, and also calls for the node network to rebuild itself
	 */
	public void destroyNode(){
		if(parentTile.getWorld().isRemote){
			//This is needed because it never gets called anywhere else in the client code
			this.updateBuildState();
		}

		Object[] backingArray = connectedNodes.toArray();
		for(Object node : backingArray){
			unlinkNode((Node)node, false, false);
		}
		parentNetwork.recalculateNetworks(this);

		//Assume this gets handled by the TileEntity.invalidate() method, otherwise this won't work!
//		if(!parentTile.getWorld().isRemote){
//			sendUpdatesToNearby();
//			for(Object node : backingArray){
//				((Node)node).sendUpdatesToNearby();
//			}
//		}
	}

	public void updateBuildState(){
		if(!isFullyBuilt){
			isFullyBuilt = attemptToBuildNodeSet();
			if(!isFullyBuilt){
				System.err.println("Node network building failed");
			}else{
//				System.out.println("Node network built successfully!");
			}
		}
	}

	/**
	 * Return true if set was built fully, false if otherwise
	 * @return
	 */
	public boolean attemptToBuildNodeSet(){
		ArrayList<BlockPos> toRemove = new ArrayList<BlockPos>();
		for(BlockPos pos : connectedNodesBlockPos){
			if(parentTile.getWorld().isBlockLoaded(pos)){
				boolean isLoaded = parentTile.getWorld().isBlockLoaded(pos);
				TileEntity tile = parentTile.getWorld().getTileEntity(pos);
				if(tile != null){
					if(tile instanceof INodeProvider){
						Node node = ((INodeProvider) tile).getNode();
						if(node != null){
							connectedNodes.add(node);
							node.connectedNodes.add(this);
							parentNetwork.mergeWithNetworks(new NodeNetwork[]{node.parentNetwork});
						}
					}
				}else{
					if(isLoaded){
						//Assume the node somehow died on its own
						toRemove.add(pos);
					}
				}
			}
		}
		connectedNodesBlockPos.remove(toRemove);
		if(connectedNodes.size() == connectedNodesBlockPos.size()){
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the network input was the same as the network the node belonged to
	 * @param newNetwork
	 * @return
	 */
	public boolean updateParentNetwork(NodeNetwork newNetwork){
		if(parentNetwork == newNetwork){
			return true;
		}
		parentNetwork = newNetwork;
		return false;
	}

	public NodeNetwork getNodeNetwork(){
		return parentNetwork;
	}

	public byte getChannel(){
		return channel;
	}

	public void setChannel(byte newChannel){
		channel = newChannel;
	}

	public void readFromNBT(NBTTagCompound compound) {
		//TODO: This might not be correct
		connectedNodesBlockPos.clear();

		int[] connectednodesarray = compound.getIntArray("connectednodesarray");

		for(int i = 0; i < connectednodesarray.length; i+=3){
			BlockPos toAdd = new BlockPos(connectednodesarray[i], connectednodesarray[i + 1], connectednodesarray[i + 2]);
			connectedNodesBlockPos.add(toAdd);
		}
		channel = compound.getByte("channel");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		int size = connectedNodesBlockPos.size();
		//3 ints for each BlockPos
		int[] arrayToWrite = new int[size * 3];
		int index = 0;
		for(BlockPos pos : connectedNodesBlockPos) {
			arrayToWrite[index] = pos.getX();
			arrayToWrite[index + 1] = pos.getY();
			arrayToWrite[index + 2] = pos.getZ();
			index += 3;
		}
		compound.setIntArray("connectednodesarray", arrayToWrite);
		compound.setByte("channel", channel);

		return compound;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Node){
			Node otherNode = (Node) o;
			if(otherNode == this || otherNode.parentTile == parentTile){
				return true;
			}
		}
		return false;
	}

}
