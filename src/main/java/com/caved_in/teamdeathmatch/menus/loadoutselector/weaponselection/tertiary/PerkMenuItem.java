package com.caved_in.teamdeathmatch.menus.loadoutselector.weaponselection.tertiary;

import com.caved_in.commons.handlers.Player.PlayerHandler;
import com.caved_in.commons.handlers.Player.PlayerWrapper;
import com.caved_in.commons.handlers.Utilities.StringUtil;
import com.caved_in.teamdeathmatch.TDMGame;
import com.caved_in.teamdeathmatch.fakeboard.FakeboardHandler;
import com.caved_in.teamdeathmatch.fakeboard.fPlayer;
import com.caved_in.teamdeathmatch.perks.Perk;
import me.xhawk87.PopupMenuAPI.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;

public class PerkMenuItem extends MenuItem {

	private com.caved_in.teamdeathmatch.perks.Perk perk;
	private int loadout;
	private boolean hasClicked = false;
	private int perkPurchaseCost = 0;
	private String perkName = "";

	public PerkMenuItem(Perk perk, int loadout, boolean isPurchased) {
		super(perk.getPerkName(), new MaterialData(Material.EXP_BOTTLE));
		this.perk = perk;
		this.perkName = perk.getPerkName();
		this.perkPurchaseCost = perk.getPurchaseCost();
		this.loadout = loadout;
		ArrayList<String> itemDescription = new ArrayList<String>();
		itemDescription.addAll(Arrays.asList(perk.getPerkDescription()));
		itemDescription.add("");
		itemDescription.add(StringUtil.formatColorCodes(isPurchased ? "&aYou've unlocked this perk!" : "&bCosts " + perkPurchaseCost + " XP to unlock"));
		this.setDescriptions(itemDescription);
	}

	@Override
	public void onClick(Player player) {
		fPlayer fPlayer = FakeboardHandler.getPlayer(player);
		if (fPlayer.hasPerk(this.perk)) {
			fPlayer.getLoadout(this.loadout).setPerk(this.perk);
			player.sendMessage(StringUtil.formatColorCodes("&aYour active perk for loadout # " + this.loadout + " is now '" + this.getText() + "'"));
			this.getMenu().closeMenu(player);
		} else {
			if (!this.hasClicked) {
				if (this.perk.isTieredPerk()) {
					if (!fPlayer.hasPerk(TDMGame.perkHandler.getPerk(perk.getPerkRequired()))) {
						player.sendMessage(StringUtil.formatColorCodes("&e" + perkName + " &cis a tiered perk, you need to purchase &e" + perk.getPerkRequired
								() + "&c before you can purchase this one"));
					} else {
						this.hasClicked = true;
						player.sendMessage(StringUtil.formatColorCodes("&eClick again to purchase &6" + this.perk.getPerkName()));
					}
				} else {
					this.hasClicked = true;
					player.sendMessage(StringUtil.formatColorCodes("&eClick again to purchase &6" + this.perk.getPerkName()));
				}
			} else {
				PlayerWrapper playerWrapper = PlayerHandler.getData(player.getName());

				if (playerWrapper.getCurrency() >= perkPurchaseCost) {
					playerWrapper.removeCurrency(perkPurchaseCost);
					PlayerHandler.updateData(playerWrapper);
					fPlayer.addPerk(perk);
					player.sendMessage(StringUtil.formatColorCodes("&bYou've purchased the &e" + this.perk.getPerkName() + "&b for &a" + this.perk
							.getPurchaseCost() + "; You have &a" + ((int) playerWrapper.getCurrency()) + " &b XP remaining."));
					this.getMenu().closeMenu(player);
				} else {
					player.sendMessage(StringUtil.formatColorCodes("&cYou don't have enough XP to unlock &e" + this.perk.getPerkName()));
				}
			}
		}
	}

}
