/* Copyright 2013 Kevin Seiden. All rights reserved.

 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0

 You are Free to:
    to Share — to copy, distribute and transmit the work
    to Remix — to adapt the work

 Under the following conditions:
    Attribution — You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial — You may not use this work for commercial purposes.

 With the understanding that:
    Waiver — Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain — Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights — In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.

 Notice — For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */

package alshain01.FlagsCore;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import alshain01.Flags.Director;
import alshain01.Flags.Flag;
import alshain01.Flags.Flags;
import alshain01.Flags.ModuleYML;
import alshain01.Flags.Registrar;

/**
 * Flags - Core Module that adds general flags to the plug-in Flags.
 * 
 * @author Alshain01
 */
public class FlagsCore extends JavaPlugin {
	/*
	 * The event handlers for the flags we created earlier
	 */
	private class CoreListener implements Listener {
		/*
		 * Handler for Enchanting
		 */
		@EventHandler(ignoreCancelled = true)
		private void onEnchantItem(EnchantItemEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("Lightning");
			if (flag != null) {
				if (!Director.getAreaAt(e.getEnchantBlock().getLocation()).getValue(flag, false)) {
					e.setExpLevelCost(0);
				}
			}
		}

		/*
		 * Handler for Zombie Door Break
		 */
		@EventHandler(ignoreCancelled = true)
		private void onEntityBreakDoor(EntityBreakDoorEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("DoorBreak");
			if (flag != null) {
				e.setCancelled(!Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
			}
		}

		/*
		 * Handler for Healing
		 */
		@EventHandler(ignoreCancelled = true)
		private void onEntityRegainHealth(EntityRegainHealthEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("Healing");
			if (flag != null && e.getEntity() instanceof Player) {
				e.setCancelled(!Director.getAreaAt(e.getEntity().getLocation())
						.getValue(flag, false));
			}
		}

		/*
		 * Handler for Hunger
		 */
		@EventHandler(ignoreCancelled = true)
		private void onFoodLevelChange(FoodLevelChangeEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("Hunger");
			if (flag != null) {
				// Make sure it's a player and make sure the hunger bar is going
				// down, not up.
				if (e.getEntity() instanceof Player
						&& e.getFoodLevel() < ((Player) e.getEntity()).getFoodLevel()) {
					e.setCancelled(!Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
				}
			}
		}

		/*
		 * Handler for Lightning
		 */
		@EventHandler(ignoreCancelled = true)
		private void onLightningStrike(LightningStrikeEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("Lightning");
			if (flag != null) {
				e.setCancelled(!Director.getAreaAt(e.getLightning().getLocation()).getValue(flag, false));
			}
		}

		/*
		 * Handler for Player Death
		 */
		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		private void onPlayerDeath(PlayerDeathEvent e) {
			final Flag flag = Flags.getRegistrar().getFlag("KeepExpOnDeath");
			if (flag != null
					&& Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false)) {
				e.setKeepLevel(true);
			}
		}
	}

	/**
	 * Called when this module is enabled
	 */
	@Override
	public void onEnable() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();

		if (!pm.isPluginEnabled("Flags")) {
			getLogger().severe("Flags was not found. Shutting down.");
			pm.disablePlugin(this);
		}

		// Connect to the data file
		final ModuleYML dataFile = new ModuleYML(this, "flags.yml");

		// Register with Flags
		final Registrar flags = Flags.getRegistrar();
		for (final String f : dataFile.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
			final ConfigurationSection data = dataFile.getModuleData().getConfigurationSection("Flag." + f);

			// The description that appears when using help commands.
			final String desc = data.getString("Description");

			final boolean def = data.getBoolean("Default");

			// Register it!
			// Be sure to send a plug-in name or group description for the help
			// command!
			// It can be this.getName() or another string.
			flags.register(f, desc, def, "Core");
		}

		// Load plug-in events and data
		Bukkit.getServer().getPluginManager()
				.registerEvents(new CoreListener(), this);
	}
}
