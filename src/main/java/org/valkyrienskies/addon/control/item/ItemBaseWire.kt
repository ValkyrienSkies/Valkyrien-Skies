package org.valkyrienskies.addon.control.item

import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import org.valkyrienskies.addon.control.ValkyrienSkiesControl
import org.valkyrienskies.addon.control.nodenetwork.WireType
import org.valkyrienskies.addon.control.tileentity.behaviour.NodeTEBehaviour
import org.valkyrienskies.addon.control.util.BaseItem
import org.valkyrienskies.mod.common.config.VSConfig
import org.valkyrienskies.mod.common.tileentity.behaviour.TEBehaviours
import kotlin.math.pow

open class ItemBaseWire(val wireType: WireType) : BaseItem(wireType.wireName, true) {

    init {
        this.maxDamage = 80
    }

    override fun addInformation(
            stack: ItemStack, player: World?,
            itemInformation: MutableList<String>,
            advanced: ITooltipFlag
    ) {
        itemInformation.add(
                TextFormatting.BLUE.toString() +
                        I18n.format("tooltip.vs_control.${wireType.wireName}")
        )
    }

    override fun onItemUse(
            player: EntityPlayer, world: World, pos: BlockPos,
            hand: EnumHand, facing: EnumFacing,
            hitX: Float, hitY: Float, hitZ: Float
    ): EnumActionResult {

        val stack = player.getHeldItem(hand)

        val tile = world.getTileEntity(pos)
        val node = TEBehaviours.getBehaviour(tile, NodeTEBehaviour::class)?.node

        if (world.isRemote && node != null) {
            return EnumActionResult.SUCCESS
        } else if (node == null) {
            return EnumActionResult.PASS
        }

        // Get the capability with the position of the last node clicked by this item
        val cap = stack.getCapability(ValkyrienSkiesControl.lastRelayCapability, null)
                ?: return EnumActionResult.PASS

        val lastPos = cap.lastRelay

        if (lastPos == null) {
            // TODO: Draw a wire in the player's hand
            cap.lastRelay = pos
            return EnumActionResult.SUCCESS
        } else {
            val distanceSq = lastPos.distanceSq(pos)
            val lastNodeTile = world.getTileEntity(lastPos)
            val lastNode = TEBehaviours.getBehaviour(lastNodeTile, NodeTEBehaviour::class)?.node

            if (lastNode == null) {
                cap.lastRelay = pos
                return EnumActionResult.SUCCESS
            }

            if (lastPos == pos) {
                cap.lastRelay = pos
                return EnumActionResult.PASS
            }

            if (distanceSq > VSConfig.relayWireLength.pow(2)) {
                player.sendMessage(TextComponentString(
                        TextFormatting.RED.toString() + I18n.format("message.vs_control.error_relay_wire_length")))
            } else if (node.isConnected(lastNode)) {
                node.disconnect(lastNode)
                // Break connection and give player the correct wire back
                // TODO: FIX
                /*val drop = ItemStack(wireType.item!!)
                if (player.inventory.addItemStackToInventory(drop)) {
                    player.dropItem(drop, false)
                }*/
                cap.lastRelay = null
            } else if (node.canConnect() && lastNode.canConnect()) {
                node.connect(lastNode, WireType.RELAY)
                stack.damageItem(1, player)
                cap.lastRelay = null
            } else {
                player.sendMessage(TextComponentString(
                        TextFormatting.RED.toString() + I18n.format("message.vs_control.error_relay_wire_limit",
                                VSConfig.networkRelayLimit)))
                cap.lastRelay = null
            }

        }

        return EnumActionResult.SUCCESS
    }
}