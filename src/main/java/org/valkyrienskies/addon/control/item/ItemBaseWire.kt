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
import org.valkyrienskies.addon.control.capability.VSControlCapabilityRegistry
import org.valkyrienskies.addon.control.nodenetwork.EnumWireType
import org.valkyrienskies.addon.control.util.BaseItem
import org.valkyrienskies.mod.common.config.VSConfig
import kotlin.math.pow

open class ItemBaseWire(val wireType: EnumWireType) : BaseItem(wireType.toString(), true) {

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
                        I18n.format("tooltip.vs_control.$wireType")
        )
    }

    override fun onItemUse(
            player: EntityPlayer, world: World, pos: BlockPos,
            hand: EnumHand, facing: EnumFacing,
            hitX: Float, hitY: Float, hitZ: Float
    ): EnumActionResult {
        val stack = player.getHeldItem(hand)

        // Get the world node network
        val network = VSControlCapabilityRegistry.getControlData(world).nodeNetwork
        // Get the node being right clicked
        val node = network.map[pos]
                ?: return EnumActionResult.PASS
        // Get the capability with the position of the last node clicked by this item
        val cap = stack.getCapability(ValkyrienSkiesControl.lastRelayCapability, null)
                ?: return EnumActionResult.PASS

        val lastPos = cap.lastRelay
        val graph = network.graph;

        if (lastPos == null) {
            // TODO: Draw a wire in the player's hand
        } else {
            val distanceSq = lastPos.distanceSq(pos)
            val lastNode = network.map[lastPos] ?: return EnumActionResult.PASS

            if (lastPos == pos) {
                cap.lastRelay = pos
                return EnumActionResult.PASS
            }

            if (distanceSq < VSConfig.relayWireLength.pow(2)) {
                if (graph.containsEdge(node, lastNode)) {
                    graph.removeEdge(node, lastNode)
                    // Break connection and give player the correct wire back
                    val drop = ItemStack(wireType.toItem())
                    if (player.inventory.addItemStackToInventory(drop)) {
                        player.dropItem(drop, false)
                    }
                } else if (graph.canConnect(node, lastNode)) {
                    graph.addEdge(node, lastNode)
                    stack.damageItem(1, player)
                } else {
                    player.sendMessage(TextComponentString(
                            TextFormatting.RED.toString() + I18n.format("message.vs_control.error_relay_wire_limit",
                                    VSConfig.networkRelayLimit)))
                }
                cap.lastRelay = null
            } else {
                player.sendMessage(TextComponentString(
                        TextFormatting.RED.toString() + I18n.format("message.vs_control.error_relay_wire_length")))
                cap.lastRelay = null
            }

        }

        return EnumActionResult.PASS
    }
}