package com.onarandombox.Rigby.MultiVerse;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class MVPlayerSession {

	@SuppressWarnings("unused")
	private Player player;
	private String portal = null;
	// Data on the Players selected locations
	private Location location1 = null;
	private Location location2 = null;
	public Location loc = null;
	
	public Boolean timer = false;
	public Boolean teleporting = false;

	private Long teleportLast = (long) 0;
	private Long infoLast = (long) 0;
	private Long alertLast = (long) 0;
	private Long messageLast = (long) 0;

	private Configuration config;

	public MVPlayerSession(Player player, Configuration config) {
		this.player = player;
		this.config = config;
		this.loc = player.getLocation();
	}

	public void setSelectedPortal(String portal) {
		this.portal = portal;
	}

	public String getSelectedPortal() {
		return this.portal;
	}

	public void setLocation1(Location location) {
		this.location1 = location;
	}

	public void setLocation2(Location location) {
		this.location2 = location;
	}

	public Location getLocation1() {
		return this.location1;
	}

	public Location getLocation2() {
		return this.location2;
	}

	public boolean compareLocation1(Location l) {
		if (location1 == null) {
			return false;
		}
		if (l.getBlockX() == location1.getBlockX()
				&& l.getBlockY() == location1.getBlockY()
				&& l.getBlockZ() == location1.getBlockZ()
				&& l.getWorld() == location1.getWorld()) {
			return true;
		}
		return false;
	}

	public boolean compareLocation2(Location l) {
		if (location2 == null) {
			return false;
		}
		if (l.getBlockX() == location2.getBlockX()
				&& l.getBlockY() == location2.getBlockY()
				&& l.getBlockZ() == location2.getBlockZ()
				&& l.getWorld() == location2.getWorld()) {
			return true;
		}
		return false;
	}

	/**
	 * Set the users portal information cooldown.
	 */
	public void setInfoCooldown() {
		Long time = (new Date()).getTime();
		this.infoLast = time;
	}

	/**
	 * getInformation Cooldown
	 */
	public boolean isInfoSendable() {
		Long time = (new Date()).getTime();
		if ((time - this.infoLast) > config.getInt("infocooldown", 2000)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set the users teleportation cooldown timer.
	 */
	public void setTPCooldown() {
		Long time = (new Date()).getTime();
		this.teleportLast = time;
	}

	/**
	 * Compare the current time against the last time used and determine whether
	 * they can use a portal again.
	 * 
	 * @param player
	 * @return
	 */
	public boolean getTeleportable() {
		Long time = (new Date()).getTime();
		if ((time - this.teleportLast) > config.getInt("tpcooldown", 5000)) {
			return true;
		} else {
			return false;
		}
	}

	public void setAlertCooldown() {
		Long time = (new Date()).getTime();
		this.alertLast = time;
	}

	public boolean getAlertable() {
		Long time = (new Date()).getTime();
		if ((time - this.alertLast) > config.getInt("alertcooldown", 5000)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Send a Message to the user, this function allows us to send messages to
	 * the user without spamming them more than once per 2 seconds.
	 * 
	 * @param t
	 */
	public void sendMessage(String t) {
		Long time = (new Date()).getTime();
		if ((time - this.messageLast) > 2000) {
			this.messageLast = time;
			this.player.sendMessage(t);
		} else {
			return;
		}
	}
}
