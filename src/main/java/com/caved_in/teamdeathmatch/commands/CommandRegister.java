package com.caved_in.teamdeathmatch.commands;

import com.caved_in.commons.commands.CommandController;
import com.caved_in.teamdeathmatch.Game;
import com.caved_in.teamdeathmatch.commands.admin.ForcemapCommand;
import com.caved_in.teamdeathmatch.commands.admin.ForcewinCommand;
import com.caved_in.teamdeathmatch.commands.admin.SetteamspawnCommand;
import com.caved_in.teamdeathmatch.commands.admin.SpawnsCommand;
import com.caved_in.teamdeathmatch.commands.player.AfkCommand;
import com.caved_in.teamdeathmatch.commands.player.KitCommand;
import com.caved_in.teamdeathmatch.commands.player.LoadoutCommand;
import com.caved_in.teamdeathmatch.commands.player.MapsCommand;
/**
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <brandon@caved.in> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return Brandon Curtis.
 * ----------------------------------------------------------------------------
 */
public class CommandRegister {
	public CommandRegister(Game Plugin) {
		CommandController.registerCommands(Plugin, new ForcemapCommand());
		CommandController.registerCommands(Plugin, new ForcewinCommand());
		CommandController.registerCommands(Plugin, new SetteamspawnCommand());
		CommandController.registerCommands(Plugin, new SpawnsCommand());
		CommandController.registerCommands(Plugin, new AfkCommand());
		CommandController.registerCommands(Plugin, new KitCommand());
		CommandController.registerCommands(Plugin, new LoadoutCommand());
		CommandController.registerCommands(Plugin, new MapsCommand());
	}
}
