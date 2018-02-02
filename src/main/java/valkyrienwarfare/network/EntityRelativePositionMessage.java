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

package valkyrienwarfare.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityRelativePositionMessage implements IMessage {

    public Integer wrapperEntityId;
    public int listSize;
    public List<Integer> entitiesToSendIDs = new ArrayList<Integer>();
    public List<Vector> entitiesRelativePosition = new ArrayList<Vector>();

    public EntityRelativePositionMessage(PhysicsWrapperEntity wrapperEntity, List<Entity> entitiesToSendRelativePosition) {
        wrapperEntityId = wrapperEntity.getEntityId();

        listSize = entitiesToSendRelativePosition.size();

        double[] wToLTransformationMatrix = wrapperEntity.wrapping.coordTransform.wToLTransform;

        for (int i = 0; i < entitiesToSendRelativePosition.size(); i++) {
            Entity entity = entitiesToSendRelativePosition.get(i);
            Vector entityPosition = new Vector(entity);
            entityPosition.transform(wToLTransformationMatrix);
            entitiesToSendIDs.add(entity.getEntityId());
            entitiesRelativePosition.add(entityPosition);
        }
    }

    public EntityRelativePositionMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        wrapperEntityId = packetBuf.readInt();
        listSize = packetBuf.readInt();

        for (int i = 0; i < listSize; i++) {
            int entityID = packetBuf.readInt();
            Vector entityLocalPosition = new Vector(packetBuf);

            entitiesToSendIDs.add(entityID);
            entitiesRelativePosition.add(entityLocalPosition);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);

        packetBuf.writeInt(wrapperEntityId);
        packetBuf.writeInt(listSize);

        for (int i = 0; i < listSize; i++) {
            int entityID = entitiesToSendIDs.get(i);
            Vector toWrite = entitiesRelativePosition.get(i);

            packetBuf.writeInt(entityID);
            toWrite.writeToByteBuf(packetBuf);
        }
    }

}
