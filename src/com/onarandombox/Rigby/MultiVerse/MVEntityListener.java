package com.onarandombox.Rigby.MultiVerse;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class MVEntityListener extends EntityListener {

	private MultiVerse plugin;

	public MVEntityListener(MultiVerse instance) {
		this.plugin = instance;
	}
	/**
	 * Event - When a Entity is Damaged, we first sort out whether it is of
	 * importance to us, such as EntityVSEntity or EntityVSProjectile.
	 * Then we grab the attacked and defender and check if its a player.
	 * Then deal with the PVP Aspect.
	 */
	public void onEntityDamage(EntityDamageEvent event){
		if (event.isCancelled()) {
			return;
		}
		Entity attacker = null;
		Entity defender = null;
		if(event instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
			attacker = sub.getDamager();
			defender = sub.getEntity();
		} else if(event instanceof EntityDamageByProjectileEvent){
			EntityDamageByProjectileEvent sub = (EntityDamageByProjectileEvent) event;
			attacker = sub.getDamager();
			defender = sub.getEntity();
		} else {
			return;
		}
		if(attacker==null || defender==null){
			return;
		}
		if (defender instanceof Player) {
			Player player = (Player) defender;
			World w = player.getWorld();

			if (attacker != null && attacker instanceof Player) {
				Player pattacker = (Player) attacker;
				if (!(this.plugin.MVWorlds.get(w.getName()).getPVP())) {
					this.plugin.playerSessions.get(pattacker.getName()).sendMessage(ChatColor.RED + "PVP is disabled in this World.");
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}
