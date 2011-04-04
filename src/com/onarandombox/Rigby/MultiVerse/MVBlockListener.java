package com.onarandombox.Rigby.MultiVerse;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.config.Configuration;

public class MVBlockListener extends BlockListener {

	MultiVerse plugin;
	Configuration config;
	Server server;
	MVUtils utils;

	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft");

	public MVBlockListener(MultiVerse instance, Configuration config) {
		this.plugin = instance;
		this.utils = new MVUtils(instance);
		this.server = instance.getServer();
		this.config = config;
	}

	/**
	 * Event - onBlockFlow - Detect whether Water/Lava is trying to flow within
	 * a portal, if so cancel it.
	 */
	@Override
	public void onBlockFlow(BlockFromToEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location l = event.getBlock().getLocation();
		if (utils.isPortal(l) != null) {
			event.setCancelled(true);
		}
	}

	/**
	 * Event - OnBlockPlace - Check if a player is placing blocks within a
	 * portal.
	 */
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		/**
		 * If a block is place we need to check if it was in the bounds of a
		 * portal If it was then we check if it was an admin or the owner which
		 * placed it. If not we cancel the event.
		 */
		String portal = utils.isPortal(event.getBlock().getLocation());
		if (portal != null) {
			MVPortal p = this.plugin.MVPortals.get(portal);
			if (!(p.getOwner().equals(event.getPlayer().getName()))
					&& !(MultiVerse.Permissions.has(event.getPlayer(),
							"multiverse.portal.override"))) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Event - onBlockDamage - Check if a player is destroying a portal or
	 * setting coordinates.
	 */
	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		/**
		 * If the block is destroyed we need to check whether it was apart of a
		 * portal. If it is then we need to check whether it was an owner or an
		 * admin of some sort that is destroying the blocks. If its not an admin
		 * or the owner then we cancel the event.
		 */
		if (event.getDamageLevel() == BlockDamageLevel.BROKEN) {
			String portal = utils.isPortal(event.getBlock().getLocation());
			if (portal != null) {
				MVPortal p = this.plugin.MVPortals.get(portal);
				if (!(p.getOwner().equals(player.getName()))
						&& !(MultiVerse.Permissions.has(player,
								"multiverse.portal.override"))) {
					event.setCancelled(true);
				}
			}
		}
		/**
		 * Following section is for when the user tries to setup portal
		 * coordinates using the portal wand.
		 */
		if (player.getItemInHand().getTypeId() == this.plugin.configMV.getInt(
				"setwand", 270)
				&& (MultiVerse.Permissions.has(player,
						"multiverse.portal.create"))) {
			String[] whitelist = this.plugin.configMV.getString("portalblocks",
					"").split(",");
			if ((whitelist.length > 0) && (whitelist[0] != "")) {
				for (int i = 0; i < whitelist.length; i++) {
					int w = Integer.valueOf(whitelist[i]);
					if (w != event.getBlock().getTypeId()) {
						return;
					}
				}
			}
			Location l = event.getBlock().getLocation();
			if (this.plugin.playerSessions.get(player.getName())
					.compareLocation1(l)) {
				return;
			}
			this.plugin.playerSessions.get(player.getName()).setLocation1(l);
			player.sendMessage("Position 1 - Set");
		}
		/**
		 * End
		 */
		/**
		 * Following section is for when the user tries to gain information on a
		 * portal by hitting it with the info wand.
		 */
		if (player.getItemInHand().getTypeId() == this.plugin.configMV.getInt(
				"infowand", 49) && event.getBlock().getTypeId() == 49) {
			if (!(this.plugin.playerSessions.get(player.getName())
					.isInfoSendable())) {
				return;
			}

			String portal = utils.isPortal(event.getBlock().getLocation());
			if (!(portal == null)) {
				MVPortal p = this.plugin.MVPortals.get(portal);

				player.sendMessage("Portal Details - ");
				player.sendMessage("Name - " + p.getName());
				player.sendMessage("Owner - " + p.getOwner());

				if (p.getDestLocation() != null) {
					String[] s = p.getDestLocation().split(":");
					if (s[0].equalsIgnoreCase("P")) {
						player.sendMessage("Destination Portal - "
								+ s[1].toString());
					}
					if (s[0].equalsIgnoreCase("W")) {
						player.sendMessage("Destination World - "
								+ s[1].toString());
					}
				}

				if (MultiVerse.useiConomy) {
					player.sendMessage("Price - " + p.getPrice());
				}

				this.plugin.playerSessions.get(player.getName())
						.setInfoCooldown();
			}
		}
		/**
		 * End
		 */
	}

	/**
	 * Event - onBlockRightClick - If a player right clicks a block check their
	 * permissions and set a Coordinate.
	 */
	@Override
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInHand().getTypeId() == this.plugin.configMV.getInt(
				"setwand", 270)
				&& (MultiVerse.Permissions.has(player,
						"multiverse.portal.create"))) {
			String[] whitelist = this.plugin.configMV.getString("portalblocks",
					"").split(",");
			if ((whitelist.length > 0) && (whitelist[0] != "")) {
				for (int i = 0; i < whitelist.length; i++) {
					int w = Integer.valueOf(whitelist[i]);
					if (w != event.getBlock().getTypeId()) {
						return;
					}
				}
			}
			Location l = event.getBlock().getLocation();
			if (this.plugin.playerSessions.get(player.getName())
					.compareLocation2(l)) {
				return;
			}
			this.plugin.playerSessions.get(player.getName()).setLocation2(l);
			player.sendMessage("Position 2 - Set");
		}
	}

	public void onBlockPhysics(BlockPhysicsEvent event) {
        if(event.isCancelled())
        {
            return;
        }

        int id = event.getChangedTypeId();

        if (id == 90 && config.getBoolean("portalanywhere", false)) {
            event.setCancelled(true);
            return;
        }
    }
}