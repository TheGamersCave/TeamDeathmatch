package com.caved_in.teamdeathmatch.listeners;

import com.caved_in.commons.items.ItemHandler;
import com.caved_in.commons.player.PlayerHandler;
import com.caved_in.commons.threading.executors.BukkitExecutors;
import com.caved_in.commons.threading.executors.BukkitScheduledExecutorService;
import com.caved_in.commons.time.Cooldown;
import com.caved_in.commons.world.WorldHandler;
import com.caved_in.teamdeathmatch.Game;
import com.caved_in.teamdeathmatch.GameMessages;
import com.caved_in.teamdeathmatch.assists.AssistManager;
import com.caved_in.teamdeathmatch.events.CustomEventHandler;
import com.caved_in.teamdeathmatch.events.GamePlayerDeathEvent;
import com.caved_in.teamdeathmatch.fakeboard.FakeboardHandler;
import com.caved_in.teamdeathmatch.fakeboard.GamePlayer;
import com.caved_in.teamdeathmatch.fakeboard.Team;
import com.caved_in.teamdeathmatch.gamehandler.GameSetupHandler;
import com.caved_in.teamdeathmatch.runnables.RestoreInventory;
import com.caved_in.teamdeathmatch.scoreboard.PlayerScoreboard;
import com.caved_in.teamdeathmatch.vote.ChatCommand;
import com.chaseoes.forcerespawn.event.ForceRespawnEvent;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static org.bukkit.util.Java15Compat.Arrays_copyOfRange;

public class BukkitListeners implements Listener {
	private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
	private Cooldown playerCooldown = new Cooldown(2);
	private Cooldown moveCooldown = new Cooldown(3);
	private Cooldown respawnInvincibilityCooldown = new Cooldown(6);
	private BukkitScheduledExecutorService sync;
	private BukkitScheduledExecutorService async;

	public BukkitListeners(Game Plugin) {
		Plugin.getServer().getPluginManager().registerEvents(this, Plugin);
		sync = BukkitExecutors.newSynchronous(Plugin);
		async = BukkitExecutors.newAsynchronous(Plugin);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		String playerName = event.getPlayer().getName();
		GamePlayer GamePlayer = FakeboardHandler.getPlayer(playerName);

		if (!moveCooldown.isOnCooldown(playerName)) {
			if (GameSetupHandler.isGameInProgress()) {
				//TODO prevent the null check from being needed
				if (GamePlayer != null && GamePlayer.isAfk()) {
					GamePlayer.setAfk(false, false);
				}
			}
			moveCooldown.setOnCooldown(playerName);
		}

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		GamePlayer GamePlayer = FakeboardHandler.getPlayer(player);
		String playerName = GamePlayer.getName();

		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (player.getItemInHand() != null && ItemHandler.itemNameContains(player.getItemInHand(), "Select & Edit Loadouts")) {
				event.setCancelled(true);
				//Open the loadout menu for the player
				GameSetupHandler.openLoadoutOptionMenu(player);
			}
		} else if (GameSetupHandler.isGameInProgress()) {
			if (!playerCooldown.isOnCooldown(playerName)) {
				if (GamePlayer.isAfk()) {
					GamePlayer.setAfk(false, false);
				}
				playerCooldown.setOnCooldown(playerName);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDied(final PlayerDeathEvent event) {
		//Check if there's a game in progress
		if (GameSetupHandler.isGameInProgress()) {
			//Create a new player death event and call it via the handler
			GamePlayerDeathEvent playerDeathEvent = new GamePlayerDeathEvent(event);
			CustomEventHandler.handleGamePlayerDeathEvent(playerDeathEvent);
		}
		event.getDrops().clear();
	}


	@EventHandler
	public void onWeaponDamageEntity(WeaponDamageEntityEvent event) {
		if (event.getVictim() instanceof Player) {
			GamePlayer playerShooter = FakeboardHandler.getPlayer(event.getPlayer());
			GamePlayer playerShot = FakeboardHandler.getPlayer((Player) event.getVictim());
			String shooterName = playerShooter.getName();
			String shotName = playerShot.getName();
			if (playerShooter.getTeam() == playerShot.getTeam() || respawnInvincibilityCooldown.isOnCooldown(shooterName) || respawnInvincibilityCooldown.isOnCooldown(shotName) || playerShooter.isAfk() || playerShot.isAfk()) {
				event.setCancelled(true);
			} else {
				AssistManager.addData(shotName, shooterName);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!GameSetupHandler.isGameInProgress()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void forceRespawn(final ForceRespawnEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		GamePlayer GamePlayer = FakeboardHandler.getPlayer(playerName);
		event.setForcedRespawn(true);
		if (GameSetupHandler.isGameInProgress()) {
			Game.runnableManager.runTaskLater(new RestoreInventory(playerName), 20);
		}
		respawnInvincibilityCooldown.setOnCooldown(playerName);
		if (GamePlayer.isAfk()) {
			GamePlayer.setAfk(false, false);
		}
	}

	@EventHandler
	public void onRespawnEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		GamePlayer gamePlayer = FakeboardHandler.getPlayer(player);
		if (GameSetupHandler.isGameInProgress()) {
			GameSetupHandler.teleportToRandomSpawn(player, gamePlayer.getTeam());
		}
		respawnInvincibilityCooldown.setOnCooldown(player.getName());
	}

	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		Player player = event.getPlayer();
		String playerName = player.getName();
		//Chat commands start with a '!' delimeter, so check if it's a chat command
		if (message.startsWith("!")) {
			event.setCancelled(true);
			String command = message;
			String[] args = PATTERN_ON_SPACE.split(message);
			if (args.length > 0) {
				command = args[0];
				try {
					args = Arrays_copyOfRange(args,1,args.length);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
//			if (message.contains(" ")) {
//				String[] tmp = message.split(" ");
//				if (tmp.length > 0) {
//					try {
//						args = new String[tmp.length - 1];
//						System.arraycopy(tmp, 1, args, 0, tmp.length);
//					} catch (ArrayIndexOutOfBoundsException ex) {
//						ex.printStackTrace();
//					}
//				}
//				command = message.split(" ")[0];
//			}
			command = StringUtils.remove(command, "!");
			if (ChatCommand.isValidCommand(command)) {
				ChatCommand chatCommand = ChatCommand.getCommand(command);
				int argsRequired = chatCommand.getMinArgs();
				if (args.length >= argsRequired) {
					chatCommand.doCommand(player, args);
				} else {
					PlayerHandler.sendMessage(player, GameMessages.INSUFFICIENT_CHAT_COMMAND_ARGUMENTS(command, argsRequired));
				}
			} else {
				PlayerHandler.sendMessage(player, GameMessages.INVALID_CHAT_COMMAND(command));
			}
		}
	}

	/*
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPreJoin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
			String playerName = event.getName();
			if (TDMGame.gunsSQL.hasData(playerName)) {
			}
		}
	}
	*/

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final String playerName = player.getName();

		PlayerHandler.removePotionEffects(player);

		ListenableFuture<Boolean> loadPlayer = async.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return FakeboardHandler.loadPlayer(playerName);
			}
		});

		Futures.addCallback(loadPlayer, new FutureCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean playerLoaded) {
				if (playerLoaded) {
					if (GameSetupHandler.isGameInProgress()) {
						GameSetupHandler.assignPlayerTeam(player);
						GameSetupHandler.teleportToRandomSpawn(player, FakeboardHandler.getPlayerTeam(player));
					} else {
						if (!PlayerHandler.getWorldName(player).equalsIgnoreCase(Game.gameMap)) {
							PlayerHandler.teleport(player, WorldHandler.getSpawn(Game.gameMap));
						}
					}
					PlayerHandler.clearInventory(player);
					GameSetupHandler.givePlayerLoadoutGem(player);
					PlayerScoreboard playerScoreboard = new PlayerScoreboard();
					player.setScoreboard(playerScoreboard.getScoreboard());
					FakeboardHandler.getPlayer(playerName).setPlayerScoreboard(playerScoreboard);
				} else {
					PlayerHandler.kickPlayer(player, GameMessages.PLAYER_DATA_LOAD_ERROR);
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				PlayerHandler.kickPlayer(player, GameMessages.PLAYER_DATA_LOAD_ERROR);
				throwable.printStackTrace();
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onReceiveName(AsyncPlayerReceiveNameTagEvent event) {
		Player playerReceiving = event.getPlayer();
		Player playerSending = event.getNamedPlayer();
		String sendingName = playerSending.getName();
		if (GameSetupHandler.isGameInProgress()) {
			Team sendingPlayerTeam = FakeboardHandler.getTeamByPlayer(playerSending);
			Team receivingPlayerTeam = FakeboardHandler.getTeamByPlayer(playerReceiving);
			if (sendingPlayerTeam != null && receivingPlayerTeam != null) {
				if (sendingPlayerTeam.getType() == receivingPlayerTeam.getType()) {
					event.setTag(ChatColor.GREEN + sendingName);
				} else {
					event.setTag(ChatColor.RED + sendingName);
				}
			} else {
				event.setTag(ChatColor.WHITE + sendingName);
			}
		} else {
			event.setTag(ChatColor.WHITE + sendingName);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent Event) {
		Player player = Event.getPlayer();
		PlayerHandler.clearInventory(player);
		FakeboardHandler.removePlayer(player);
	}
}
