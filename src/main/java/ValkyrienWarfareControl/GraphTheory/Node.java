package ValkyrienWarfareControl.GraphTheory;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class Node {

	public final TileEntity parentTile;
	//No duplicate connections
	public HashSet<Node> connectedNodes;
	public HashSet<BlockPos> connectedNodesBlockPos;
	private boolean isFullyBuilt = false;

	public Node(TileEntity parent){
		parentTile = parent;
		connectedNodes = new HashSet<Node>();
		connectedNodesBlockPos = new HashSet<BlockPos>();
	}

	public void linkNode(Node other){
		updateBuildState();
		connectedNodes.add(other);
		other.connectedNodes.add(this);
		connectedNodesBlockPos.add(other.parentTile.getPos());
		other.connectedNodesBlockPos.add(this.parentTile.getPos());
	}

	public void unlinkNode(Node other){
		updateBuildState();
		connectedNodes.remove(other);
		other.connectedNodes.remove(this);
		connectedNodesBlockPos.remove(other.parentTile.getPos());
		other.connectedNodesBlockPos.remove(this.parentTile.getPos());
	}

	/**
	 * Destroys all other connections to this node from other nodes, and also calls for the node network to rebuild itself
	 */
	public void destroyNode(){
		Object[] backingArray = connectedNodes.toArray();
		for(Object node : backingArray){
			unlinkNode((Node)node);
		}
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

	public void readFromNBT(NBTTagCompound compound) {
		int[] connectednodesarray = compound.getIntArray("connectednodesarray");

		for(int i = 0; i < connectednodesarray.length; i+=3){
			BlockPos toAdd = new BlockPos(connectednodesarray[i], connectednodesarray[i + 1], connectednodesarray[i + 2]);
			connectedNodesBlockPos.add(toAdd);
		}
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
