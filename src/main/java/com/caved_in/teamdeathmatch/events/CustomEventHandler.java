package com.caved_in.teamdeathmatch.events;

import com.caved_in.commons.player.PlayerHandler;
import com.caved_in.commons.player.PlayerWrapper;
import com.caved_in.teamdeathmatch.TDMGame;
import com.caved_in.teamdeathmatch.fakeboard.FakeboardHandler;
import com.caved_in.teamdeathmatch.fakeboard.GamePlayer;
import com.caved_in.teamdeathmatch.fakeboard.Team;
import com.caved_in.teamdeathmatch.gamehandler.GameSetupHandler;
import com.caved_in.teamdeathmatch.gamehandler.KillstreakHandler;
import com.caved_in.teamdeathmatch.guns.GunWrapper;
import com.caved_in.teamdeathmatch.perks.Perk;
import com.caved_in.teamdeathmatch.runnables.AssistAggregator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;

public class CustomEventHandler {

	private static void callEvent(Event event) {
		Bukkit.getServer().getPluginManager().callEvent(event);
	}

	public static void handleChatCommandEvent(ChatCommandEvent event) {
		callEvent(event);
	}

	public static void handleGamePlayerDeathEvent(GamePlayerDeathEvent event) {
		callEvent(event);

		Player player = event.getEntity();
		GamePlayer gamePlayerKilled = event.getGamePlayer();
		//If the inventory is to be kept, then set the restoration inventory
		if (event.isInventoryKept()) {
			gamePlayerKilled.setDeathInventory(player.getInventory().getContents());
		}
		//Get the player who killed this player
		Player playerKiller = player.getKiller();
		if (playerKiller != null) {
			GamePlayer killingPlayer = FakeboardHandler.getPlayer(playerKiller);
			if (killingPlayer != null && gamePlayerKilled != null) {
				//Get the team of the killing player
				Team killerTeam = FakeboardHandler.getTeam(killingPlayer.getTeam());
				//Add score to the killing team
				killerTeam.addTeamScore(1);
				//Add a death to the player who was killed
				gamePlayerKilled.addDeath();
				//Add +1 score to the player who killed
				killingPlayer.addScore(1);
				//Add killstreak progress to the player who killed
				killingPlayer.addKillstreak(1);
				KillstreakHandler.HandleKillStreak(killingPlayer);
				gamePlayerKilled.resetKillstreak();

				final String killerName = killingPlayer.getName();
				final String killedName = gamePlayerKilled.getName();

				TDMGame.runnableManager.runTaskLater(new AssistAggregator(killedName, killerName), 5);
			}
		}
		PlayerHandler.clearInventory(player);
		player.setScoreboard(gamePlayerKilled.getPlayerScoreboard().getScoreboard());
	}

	public static void handleGamePlayerRespawnEvent(GamePlayerRespawnEvent event) {
		callEvent(event);

	}

	public static void handleGameEndEvent(GameEndEvent event) {
		callEvent(event);

	}

	public static void handleGunPurchaseEvent(GunPurchaseEvent event) {
		//Call the event
		callEvent(event);
		//Assure it's not cancelled
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			GamePlayer gamePlayer = event.getGamePlayer();
			GunWrapper gunData = event.getGun();
			String gunID = gunData.getGunName();
			//Get the wrapped player data from commons
			PlayerWrapper playerWrapper = PlayerHandler.getData(player.getName());
			double playerBalance = playerWrapper.getCurrency();
			//Check if the player has enough XP to purchase the gun
			if (playerBalance >= gunData.getGunPrice()) {
				playerWrapper.removeCurrency(gunData.getGunPrice());
				PlayerHandler.updateData(playerWrapper);
				gamePlayer.unlockGun(gunID);
				PlayerHandler.sendMessage(player, String.format("&bYou've unlocked the &e%s&b! You have &a%s&b Tunnels XP Remaining", gunID, (int) playerWrapper.getCurrency()));
				gamePlayer.getLoadout(event.getLoadoutNumber()).setPrimary(gunID);
				PlayerHandler.sendMessage(player, String.format("&aThe &e%s&a is now your primary weapon for loadout #&e%s", gunID, event.getLoadoutNumber()));
			} else {
				PlayerHandler.sendMessage(player, "&cYou don't have enough XP to unlock this.");
			}
		}
	}

	public static void handleLoadoutEditEvent(LoadoutEditEvent event) {
		callEvent(event);

	}

	public static void handleLoadoutSelectEvent(LoadoutSelectEvent event) {
		callEvent(event);
		//If the events not cancelled
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			GamePlayer gamePlayer = event.getGamePlayer();
			int selectedLoadout = event.getLoadout().getNumber();
			//Clear the players inventory
			PlayerHandler.clearInventory(player, false);
			//Set the active loadout
			gamePlayer.setActiveLoadout(selectedLoadout);
			//Give them their primary and secondary weapons
			TDMGame.crackShotAPI.giveWeapon(player, gamePlayer.getPrimaryGunID(), 1);
			TDMGame.crackShotAPI.giveWeapon(player, gamePlayer.getSecondaryGunID(), 1);
			Perk activePerk = gamePlayer.getActivePerk();
			//If they've got an active perk and it isn't nothing
			if (activePerk != null && !activePerk.getPerkName().equalsIgnoreCase("Nothing")) {
				for (PotionEffect Effect : gamePlayer.getActivePerk().getEffects()) {
					player.addPotionEffect(Effect);
				}
			}
			GameSetupHandler.givePlayerLoadoutGem(player);
		}
	}

	public static void handlePlayerAfkEvent(PlayerAfkEvent event) {

	}
}
