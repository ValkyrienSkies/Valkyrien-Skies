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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import valkyrienwarfare.mod.multithreaded.VWThread;
import valkyrienwarfare.mod.physmanagement.interaction.ShipNameUUIDData;
import valkyrienwarfare.mod.physmanagement.interaction.ShipUUIDToPosData;
import valkyrienwarfare.ship_handling.IHasShipManager;
import valkyrienwarfare.ship_handling.WorldServerShipManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VWCommandHelp extends CommandBase {

	public static final List<String> COMMANDS = new ArrayList<String>();

	static {
		COMMANDS.add("/physsettings");
		COMMANDS.add("/airshipsettings");
		COMMANDS.add("/airshipmappings");
		COMMANDS.add("/vw tps");
	}

	@Override
	public String getName() {
		return "vw";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/vw       See entire list of commands for Valkyrien Warfare";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(new TextComponentString("All ValkyrienWarfare Commands"));

			for (String command : COMMANDS) {
				sender.sendMessage(new TextComponentString(command));
			}

			sender.sendMessage(new TextComponentString("To see avaliable subcommands, type /command help"));
		} else if (args.length == 1 && args[0].toLowerCase().equals("tps")) {
			World world = sender.getEntityWorld();
			VWThread worldPhysicsThread = ((WorldServerShipManager) IHasShipManager.class.cast(world).getManager()).getPhysicsThread();
			if (worldPhysicsThread != null) {
				long averagePhysTickTimeNano = worldPhysicsThread.getAveragePhysicsTickTimeNano();
				double ticksPerSecond = 1000000000D / ((double) averagePhysTickTimeNano);
				double ticksPerSecondTwoDecimals = Math.floor(ticksPerSecond * 100) / 100;
				sender.sendMessage(new TextComponentString("Player world: " + ticksPerSecondTwoDecimals + " physics ticks per second"));
			}
		} else if (args[0].toLowerCase().equals("listships")) {
			ShipNameUUIDData shipData = ShipNameUUIDData.get(sender.getEntityWorld());
			String delimitedListOfAllShipNames = String.join("\n", shipData.shipNameToLongMap.keySet());
			sender.sendMessage(new TextComponentString(delimitedListOfAllShipNames));
		} else if (args[0].toLowerCase().equals("getshiplocation")) {
			if (args.length == 1) {
				sender.sendMessage(new TextComponentString("Please include a ship name! /vw getshiplocation <shipname>"));
				return;
			}
			World world = sender.getEntityWorld();
			ShipNameUUIDData shipData = ShipNameUUIDData.get(world);
			Long shipUUID = shipData.shipNameToLongMap.get(
					// Combine remaining arguments
					String.join("", Arrays.copyOfRange(args, 1, args.length))
			);

			if (shipUUID == null) {
				sender.sendMessage(new TextComponentString("Invalid ship name!"));
				return;
			}

			ShipUUIDToPosData shipPosData = ShipUUIDToPosData.getShipUUIDDataForWorld(sender.getEntityWorld());
			ShipUUIDToPosData.ShipPositionData shipPos = shipPosData.getShipPositionData(shipUUID);
			sender.sendMessage(new TextComponentString(
					String.format(
							"Coordinates: %.1f, %.1f, %.1f",
							shipPos.getPosX(),
							shipPos.getPosY(),
							shipPos.getPosZ()
					)
			));
		}
	}
}
