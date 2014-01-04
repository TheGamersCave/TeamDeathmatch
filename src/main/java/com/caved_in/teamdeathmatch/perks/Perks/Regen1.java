package com.caved_in.teamdeathmatch.perks.Perks;

import com.caved_in.teamdeathmatch.handlers.misc.TimeUtils.TimeType;
import com.caved_in.teamdeathmatch.perks.Perk;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class Regen1 extends Perk {

	public Regen1() {
		super("Pain Killer", 8000, new String[]{ChatColor.YELLOW + "Nobody has time for weakness", ChatColor.YELLOW + "Especially when you're kicking ass!",
				"", ChatColor.GREEN + "Regenerate Health Extremmely fast when active"});
	}

	@Override
	public List<PotionEffect> getEffects() {
		return Arrays.asList(new PotionEffect[]{new PotionEffect(PotionEffectType.REGENERATION, TimeUtils.getTimeInTicks(20, TimeType.Minute), 1)});
	}

}
