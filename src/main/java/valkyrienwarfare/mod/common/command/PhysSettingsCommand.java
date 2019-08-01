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

package valkyrienwarfare.mod.common.command;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import valkyrienwarfare.mod.common.config.VWConfig;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.math.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhysSettingsCommand extends CommandBase {

    public static final List<String> COMPLETED_OPTIONS = new ArrayList<String>();

    static {
        COMPLETED_OPTIONS.add("gravityvector");
        COMPLETED_OPTIONS.add("maxshipsize");
        COMPLETED_OPTIONS.add("physicsspeed");
        COMPLETED_OPTIONS.add("dogravity");
        COMPLETED_OPTIONS.add("dophysicsblocks");
        COMPLETED_OPTIONS.add("doairshiprotation");
        COMPLETED_OPTIONS.add("doairshipmovement");
        COMPLETED_OPTIONS.add("save");
        COMPLETED_OPTIONS.add("doethereumlifting");
        COMPLETED_OPTIONS.add("restartcrashedphysics");
    }

    @Override
    public String getName() {
        return "physsettings";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/physsettings <setting name> [value]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("Avaliable physics Commands:"));
            for (String command : COMPLETED_OPTIONS) {
                sender.sendMessage(new TextComponentString(command));
            }
            return;
        }
        String key = args[0];
        if (key.equals("maxshipsize")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString("maxshipsize=" + VWConfig.maxShipSize + " (Default: 15000)"));
                return;
            } else if (args.length == 2) {
                int value = Integer.parseInt(args[1]);
                VWConfig.maxShipSize = value;
                sender.sendMessage(new TextComponentString("Set maximum ship size to " + value));
                return;
            }
        } else if (key.equals("gravityvector")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString("gravityvector=" + VWConfig.gravity().toRoundedString() + " (Default: <0,-9.8,0>)"));
                return;
            } else if (args.length == 4) {
                double x, y, z;
                try {
                    if (args[1] != null && args[2] != null && args[3] != null) {
                        x = Double.parseDouble(args[1]);
                        y = Double.parseDouble(args[2]);
                        z = Double.parseDouble(args[3]);
                    } else {
                        sender.sendMessage(new TextComponentString(
                                "Usage: /physsettings gravityVector <x> <y> <z>"));
                        return;
                    }

                    sender.sendMessage(new TextComponentString(
                            "physics gravity set to " + x + ", " + y + ", " + z + " (Default: <0,-9.8,0>)"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponentString("That's not a valid number.."));
                }

                return;
            } else {
                sender.sendMessage(new TextComponentString(
                        "Usage: /physsettings gravityVector <x> <y> <z>"));
            }
        } else if (key.equals("physicsspeed")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "physicsspeed=" + VWConfig.physSpeed + " (Default: 100%)"));
                return;
            } else if (args.length == 2) {
                double value = Double.parseDouble(args[1].replace('%', ' '));
                if (value < 0 || value > 1000) {
                    sender.sendMessage(new TextComponentString("Please enter a value between 0 and 1000"));
                    return;
                }
                VWConfig.physSpeed = (value / 10000D);
                sender.sendMessage(new TextComponentString("Set physicsspeed to " + value + " percent"));
                return;
            }
        } else if (key.equals("dogravity")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "dogravity=" + VWConfig.doGravity + " (Default: true)"));
                return;
            } else if (args.length == 2) {
                boolean value = Boolean.parseBoolean(args[1]);
                VWConfig.doGravity = value;
                sender.sendMessage(new TextComponentString(
                        "Set dogravity to " + (VWConfig.doGravity ? "enabled" : "disabled")));
                return;
            }
        } else if (key.equals("dophysicsblocks")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "dophysicsblocks=" + VWConfig.doPhysicsBlocks + " (Default: true)"));
                return;
            } else if (args.length == 2) {
                boolean value = Boolean.parseBoolean(args[1]);
                VWConfig.doPhysicsBlocks = value;
                sender.sendMessage(new TextComponentString(
                        "Set dophysicsblocks to " + (VWConfig.doPhysicsBlocks ? "enabled" : "disabled")));
                return;
            }
        } else if (key.equals("doairshiprotation")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "doairshiprotation=" + VWConfig.doAirshipRotation + " (Default: true)"));
                return;
            } else if (args.length == 2) {
                boolean value = Boolean.parseBoolean(args[1]);
                VWConfig.doAirshipRotation = value;
                sender.sendMessage(new TextComponentString(
                        "Set doairshiprotation to " + (VWConfig.doAirshipRotation ? "enabled" : "disabled")));
                return;
            }
        } else if (key.equals("doairshipmovement")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "doairshipmovement=" + VWConfig.doAirshipMovement + " (Default: true)"));
                return;
            } else if (args.length == 2) {
                boolean value = Boolean.parseBoolean(args[1]);
                VWConfig.doAirshipMovement = value;
                sender.sendMessage(new TextComponentString(
                        "Set doairshipmovement to " +
                                (VWConfig.doAirshipMovement ? "enabled" : "disabled")));
                return;
            }
        } else if (key.equals("doethereumlifting")) {
            if (args.length == 1) {
                sender.sendMessage(new TextComponentString(
                        "doethereumlifting=" + VWConfig.doEthereumLifting + " (Default: true)"));
                return;
            } else if (args.length == 2) {
                boolean value = Boolean.parseBoolean(args[1]);
                VWConfig.doEthereumLifting = value;
                sender.sendMessage(new TextComponentString(
                        "Set doethereumlifting to " + 
                                (VWConfig.doEthereumLifting ? "enabled" : "disabled")));
                return;
            }
        } else if (key.equals("save")) {
            sender.sendMessage(new TextComponentString("Saved physics settings"));
            return;
        } else {
            sender.sendMessage(new TextComponentString("Available physics Commands:"));
            for (String command : COMPLETED_OPTIONS) {
                sender.sendMessage(new TextComponentString(command));
            }
        }

        VWConfig.sync();

        sender.sendMessage(new TextComponentString(this.getUsage(sender)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> possibleArgs = new ArrayList<String>(COMPLETED_OPTIONS);
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
