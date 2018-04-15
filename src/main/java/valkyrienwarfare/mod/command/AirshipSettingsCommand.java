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

package valkyrienwarfare.mod.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class AirshipSettingsCommand extends CommandBase {

    public static final List<String> COMPLETION_OPTIONS = new ArrayList<String>();

    static {
        COMPLETION_OPTIONS.add("transfer");
        COMPLETION_OPTIONS.add("allowplayer");
        COMPLETION_OPTIONS.add("claim");
    }

    //Ripoff of world.rayTraceBlocks(), blame LEX and his Side code
    public static RayTraceResult rayTraceBothSides(EntityPlayer player, double blockReachDistance, float partialTicks) {
        Vec3d vec3d = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d vec3d1 = player.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return player.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

    @Override
    public String getName() {
        return "airshipsettings";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/airshipsettings <setting name> [value]" + "\n" + "Avaliable Settings: [transfer, allowplayer, claim]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("You need to be a player to do that!"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
            return;
        }

        EntityPlayer p = (EntityPlayer) sender;
        //This method has an @SIDE.CLIENT, and it broke all the commands on servers!
//		BlockPos pos = p.rayTrace(p.isCreative() ? 5.0 : 4.5, 1).getBlockPos();

        BlockPos pos = rayTraceBothSides(p, p.isCreative() ? 5.0 : 4.5, 1).getBlockPos();

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(p.getEntityWorld(), pos);

        if (wrapper == null) {
            sender.sendMessage(new TextComponentString("You need to be looking at an airship to do that!"));
            return;
        }
        if (p.entityUniqueID.toString().equals(wrapper.wrapping.creator)) {
            if (args[0].equals("transfer")) {
                if (args.length == 1) {
                    return;
                }
                if (!args[1].isEmpty()) {
                    EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
                    if (target == null) {
                        p.sendMessage(new TextComponentString("That player is not online!"));
                        return;
                    }
                    switch (wrapper.wrapping.changeOwner(target)) {
                        case ERROR_IMPOSSIBLE_STATUS:
                            p.sendMessage(new TextComponentString("An error occured, please report to mod devs"));
                            break;
                        case ERROR_NEWOWNER_NOT_ENOUGH:
                            p.sendMessage(new TextComponentString("That player doesn't have enough free airship slots!"));
                            break;
                        case SUCCESS:
                            p.sendMessage(new TextComponentString("Success! " + target.getName() + " is the new owner of this airship!"));
                            break;
                        case ALREADY_CLAIMED:
                            p.sendMessage(new TextComponentString("Airship already claimed"));
                            break;
                    }
                    return;
                }
            } else if (args[0].equals("allowplayer")) {
                if (args.length == 1) {
                    StringBuilder result = new StringBuilder("<");
                    Iterator<String> iter = wrapper.wrapping.allowedUsers.iterator();
                    while (iter.hasNext()) {
                        result.append(iter.next() + (iter.hasNext() ? ", " : ">"));
                    }
                    p.sendMessage(new TextComponentString(result.toString()));
                    return;
                }
                if (!args[1].isEmpty()) {
                    EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
                    if (target == null) {
                        p.sendMessage(new TextComponentString("That player is not online!"));
                        return;
                    }
                    if (target.entityUniqueID.toString().equals(wrapper.wrapping.creator)) {
                        p.sendMessage(new TextComponentString("You can't add yourself to your own airship!"));
                        return;
                    }
                    wrapper.wrapping.allowedUsers.add(target.entityUniqueID.toString());
                    p.sendMessage(new TextComponentString("Success! " + target.getName() + " can now interact with this airship!"));
                    return;
                }
            }
        } else {
            if (wrapper.wrapping.creator == null || wrapper.wrapping.creator.trim().isEmpty()) {
                if (args.length == 1 && args[0].equals("claim")) {
                    wrapper.wrapping.creator = p.entityUniqueID.toString();
                    p.sendMessage(new TextComponentString("You've successfully claimed an airship!"));
                    return;
                }
            }
            p.sendMessage(new TextComponentString("You need to be the owner of an airship to change airship settings!"));
        }
        if (args[0].equals("help")) {
            for (String command : COMPLETION_OPTIONS) {
                sender.sendMessage(new TextComponentString(command));
            }
        }

        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> possibleArgs = new ArrayList<String>(COMPLETION_OPTIONS);

            for (Iterator<String> iterator = possibleArgs.iterator(); iterator.hasNext(); ) { //Don't like this, but I have to because concurrentmodificationexception
                if (!iterator.next().startsWith(args[0])) {
                    iterator.remove();
                }
            }

            return possibleArgs;
        } else if (args.length == 2) {
            if (args[0].startsWith("do")) {
                if (args[1].startsWith("t")) {
                    return Lists.newArrayList("true");
                } else if (args[1].startsWith("f")) {
                    return Lists.newArrayList("false");
                } else {
                    return Lists.newArrayList("true", "false");
                }
            }
        }

        return null;
    }
}
