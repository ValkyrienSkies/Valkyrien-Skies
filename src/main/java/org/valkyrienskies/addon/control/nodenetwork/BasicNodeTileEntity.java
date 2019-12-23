package org.valkyrienskies.addon.control.nodenetwork;

import gigaherz.graph.api.Graph;
import gigaherz.graph.api.Mergeable;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public abstract class BasicNodeTileEntity extends TileEntity implements IVSNodeProvider, ITickable {

    private final VSNode_TileEntity tileNode;
    private boolean firstUpdate;

    public BasicNodeTileEntity() {
        this.tileNode = new VSNode_TileEntity(this, getMaximumConnections());
        this.firstUpdate = true;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0,
            writeToNBT(new NBTTagCompound()));
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        tileNode.writeToNBT(compound);
        return super.writeToNBT(compound);
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
    public VSNode_TileEntity getNode() {
        return tileNode;
    }

    @Override
    public void invalidate() {
        VSNode_TileEntity toInvalidate = getNode();
        Graph graph = toInvalidate.getGraph();
        if (graph != null) {
            graph.remove(toInvalidate);
            toInvalidate.breakAllConnections();
        }
        // The Node just got destroyed
        this.tileEntityInvalid = true;
        toInvalidate.invalidate();
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

    /**
     * @return The maximum number of nodes this tile entity can be connected to.
     */
    protected int getMaximumConnections() {
        return 1;
    }

    private void init() {
        if (tileNode.getGraph() == null) {
            return;
        }
        tileNode.getGraph().addNeighours(tileNode, tileNode.getNeighbours());
    }

    @Override
    public Iterable<IVSNode> getNetworkedConnections() {
        return () -> new IteratorCaster(tileNode.getGraph()
            .getObjects()
            .iterator());
    }

    public static class GraphData implements Mergeable<GraphData> {

        private static volatile int sUid = 0;

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

    private static class IteratorCaster implements Iterator<IVSNode> {

        private final Iterator toCast;

        private IteratorCaster(Iterator toCast) {
            this.toCast = toCast;
        }

        @Override
        public boolean hasNext() {
            return toCast.hasNext();
        }

        @Override
        public IVSNode next() {
            return (IVSNode) toCast.next();
        }
    }

}
