package com.onarandombox.Rigby.MultiVerse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MVTeleport {

	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft");

	private MultiVerse plugin;

	public MVTeleport(MultiVerse instance) {
		this.plugin = instance;
	}

	/**
	 * This function gets a safe place to teleport to when teleporting to a
	 * Portal.
	 * 
	 * @param l
	 * @param p
	 * @return
	 */

	public Location getPortalDestination(Location l, Player p) {
		World world = l.getWorld();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		int y = l.getBlockY();

		int distance = 1;
		Boolean notSafe = true;
		Location d = null;
		while (notSafe) {
			if (!(y < 128)) {
				notSafe = false;
				break;
			}
			for (int xx = (x - distance); xx <= (x + distance); xx++) {
				for (int zz = (z - distance); zz <= (z + distance); zz++) {
					if (this.plugin.utils.isPortal(new Location(world, xx, y,
							zz, (float) 0.0, (float) 0.0)) == null) {
						if (!(blockIsNotSafe(world, xx, y,
								zz)) && d == null) {
							Double dx = (xx) + 0.5;
							Double dz = (zz) + 0.5;
							notSafe = false;
							d = new Location(world, dx, y, dz, l.getYaw(),
									l.getPitch());
							break;
						}
					}
				}
			}

			if (distance == 5) {
				y++;
				distance = 1;
			} else {
				distance++;
			}
		}
		return d;
	}

	/**
	 * This function gets a safe place to teleport to.
	 * 
	 * @param location
	 * @param player
	 * @return
	 */
	public Location getDestination(Location location, Player player) {
		World world = location.getWorld();
		double x = location.getX() + 0.5;
		double y = location.getY();
		double z = location.getZ() + 0.5;

		if (y < 1 && world.getEnvironment() == Environment.NORMAL)
			y = 1;

		while (this.blockIsAboveAir(world, x, y, z)) {
			y--;
		}
		while (this.blockIsNotSafe(world, x, y, z)) {
			y++;
			if (y == 110) {
				y = 1;
				x = x + 1;
				z = z + 1;
			}
		}
		return new Location(world, x, y, z, location.getYaw(),
				location.getPitch());
	}

	/**
	 * This function checks whether the block at the given coordinates are above
	 * air or not.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private boolean blockIsAboveAir(World world, double x, double y, double z) {
		return (world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1),
				(int) Math.floor(z)).getType() == Material.AIR);
	}

	/**
	 * This function checks whether the block at the coordinates given is safe
	 * or not by checking for Laval/Fire/Air etc.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean blockIsNotSafe(World world, double x, double y, double z) {
		if ((world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1),
				(int) Math.floor(z)).getType() == Material.LAVA))
			return true;
		if ((world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1),
				(int) Math.floor(z)).getType() == Material.STATIONARY_LAVA))
			return true;
		if ((world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1),
				(int) Math.floor(z)).getType() == Material.FIRE))
			return true;
		if (world.getBlockAt((int) Math.floor(x), (int) Math.floor(y),
				(int) Math.floor(z)).getType() != Material.AIR
				|| world.getBlockAt((int) Math.floor(x),
						(int) Math.floor(y + 1), (int) Math.floor(z)).getType() != Material.AIR)
			return true;
		if (blockIsAboveAir(world, x, y, z))
			return true;

		return false;
	}

	/**
	 * Check whether it is a Portal and then find out if it has a destination.
	 * If it does we need to return the location or Null.
	 * 
	 * @param pl
	 * @param ptest
	 * @param p
	 * @return
	 */
	public Location portalDestination(Player pl, String ptest, MVPortal p) {
		if (p.getDestLocation() != null) {
			String[] dest = p.getDestLocation().split(":");
			if (dest[0].toString().equalsIgnoreCase("P")) {
				if (this.plugin.MVPortals.containsKey(dest[1].toString())) {
					MVPortal pd = this.plugin.MVPortals.get(dest[1].toString());
					Vector v = pd.min;
					Location l = new Location(pd.getWorld(), v.getX(),
							v.getY(), v.getZ(), (float) 0.0, (float) 0.0);
					return getPortalDestination(l, pl);
				} else {
					return null;
				}
			}
			if (dest[0].toString().equalsIgnoreCase("W")) {
				World world = this.plugin.getServer().getWorld(
						dest[1].toString());
				if (world != null) {
					if (dest[2].toString().equalsIgnoreCase("SPAWN")) {
						Location l = this.plugin.MVWorlds.get(world.getName())
								.getSpawnLocation();
						return getDestination(l, pl);
					}
					if (dest.length == 2) {
						Location l = this.plugin.MVWorlds.get(world.getName())
								.getSpawnLocation();
						return getDestination(l, pl);
					}
					if (dest.length > 2) {
						// log.info(dest[2] + " " + dest[3] + " " + dest[4] +
						// " " + dest[5] + " " + dest[6]);
						int x = Integer.valueOf(dest[2]);
						int y = Integer.valueOf(dest[3]);
						int z = Integer.valueOf(dest[4]);
						float yaw = Float.valueOf(dest[5]);
						float pitch = Float.valueOf(dest[6]);
						Location l = new Location(world, x, y, z, yaw, pitch);
						return getDestination(l, pl);
					}
				}
			}
		}
		return null;
	}

	/**
	 * This is ran if no portal is found, we check if there's a sign nearby and
	 * use that to find the destination.
	 * 
	 * @param pl
	 * @return
	 */
	public Location portalSignMethod(Player pl) {
		Location l = pl.getLocation();
		Block b = pl.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(),
				l.getBlockZ());
		List<Sign> s = new ArrayList<Sign>();
		if (b.getType().equals(Material.PORTAL)) {
			for (int x = -2; x <= 2; x++) {
				for (int y = -1; y <= 3; y++) {
					for (int z = -2; z <= 2; z++) {
						BlockState block = b.getRelative(x, y, z).getState();
						if (block.getType() == Material.WALL_SIGN) {
							s.add((Sign) block);
						}
					}
				}
			}

			if (s.size() != 0) {
				for (int i = 0; i < s.size(); i++) {
					Sign sign = s.get(i);
					if ((sign.getLine(1).equalsIgnoreCase("[multiverse]") || sign
							.getLine(1).equalsIgnoreCase("[mv]"))
							&& (sign.getLine(2).length() > 0)) {
						World world = this.plugin.getServer().getWorld(
								sign.getLine(2).toString());
						if (world != null) {
							return getDestination(
									this.plugin.MVWorlds.get(world.getName())
											.getSpawnLocation(), pl);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * This is ran if a Portal is Found and no destination has been assigned
	 * yet. The server also has to have "splike" enabled.
	 * 
	 * @param pl
	 * @return
	 */
	public Location portalSPNether(Player pl) {
		String nether = this.plugin.configMV.getString("nether", "");
		String world = this.plugin.configMV.getString("default", "");
		if (nether.length() == 0 || world.length() == 0) {
			return null;
		}
		if (this.plugin.getServer().getWorld(nether) == null
				|| this.plugin.getServer().getWorld(world) == null) {
			return null;
		}
		World netherw = this.plugin.getServer().getWorld(nether);
		World worldw = this.plugin.getServer().getWorld(world);

		Location l = pl.getLocation();
		Block b = pl.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(),
				l.getBlockZ());
		if (b.getType().equals(Material.PORTAL)) {

			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();

			int factor = 8;
			World w;
			Location p;
			if (pl.getWorld().getEnvironment() == Environment.NORMAL
					&& pl.getWorld().getName().equalsIgnoreCase(world)) {
				w = netherw;
				x = (x / factor);
				z = (z / factor);
				l = new Location(w, x, y, z);
				p = nearbyPortal(l, pl);
			} else if (pl.getWorld().getEnvironment() == Environment.NETHER
					&& pl.getWorld().getName().equals(nether)) {
				w = worldw;
				x = (x * factor);
				z = (z * factor);
				l = new Location(w, x, y, z);
				p = l;
			} else {
				return null;
			}

			if (p != null) {
				return getPortalDestination(p, pl);
			}
			l = getDestination(l, pl);

			if (this.plugin.configMV.getBoolean("autobuild", false)) {
				this.plugin.utils.buildPortal(l);
			}
			return l;
		}
		return null;
	}

	/**
	 * Function to grab a nearby portal.
	 * 
	 * @param l
	 * @param pl
	 * @return
	 */
	private Location nearbyPortal(Location l, Player pl) {
		World world = l.getWorld();

		for (int x = l.getBlockX() - 16; x <= l.getBlockX() + 16; ++x)
			for (int z = l.getBlockZ() - 16; z <= l.getBlockZ() + 16; ++z)
				for (int y = 127; y >= 0; --y) {
					Block b = world.getBlockAt(x, y, z);
					if (b.getType().equals(Material.PORTAL)) {
						return new Location(world, x, y - 2, z);
					}
				}
		return null;
	}

	/**
	 * Check if the Player has the permissions to enter this world.
	 * 
	 * @param p
	 * @param w
	 * @return
	 */
	public Boolean canEnterWorld(Player p, World w) {
		List<String> whiteList = this.plugin.MVWorlds.get(w.getName()).joinWhitelist;
		List<String> blackList = this.plugin.MVWorlds.get(w.getName()).joinBlacklist;
		String group = MultiVerse.Permissions.getGroup(w.getName(), p.getName());
		
		boolean returnValue = true;

		if (whiteList.size() > 0)
			returnValue = false;

		for (int i = 0; i < whiteList.size(); i++){
			if (whiteList.get(i).contains("g:")	&& group.equalsIgnoreCase(whiteList.get(i).split(":")[1])) {
				returnValue = true;
				break;
			}
		}

		for (int i = 0; i < blackList.size(); i++){
			if (blackList.get(i).contains("g:") && group.equalsIgnoreCase(blackList.get(i).split(":")[1])) {
				returnValue = false;
				break;
			}
		}

		for (int i = 0; i < whiteList.size(); i++){
			if (whiteList.get(i).equalsIgnoreCase(p.getName())) {
				returnValue = true;
				break;
			}
		}

		for (int i = 0; i < blackList.size(); i++){
			if (blackList.get(i).equalsIgnoreCase(p.getName())) {
				returnValue = false;
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Check if a Player can teleport to the Destination world from there
	 * current world. This checks against the Worlds Blacklist
	 * 
	 * @param p
	 * @param w
	 * @return
	 */
	public Boolean canTravelFromWorld(Player p, World w) {
		List<String> blackList = this.plugin.MVWorlds.get(w.getName()).worldBlacklist;

		boolean returnValue = true;

		if (blackList.size() == 0)
			returnValue = true;

		for (int i = 0; i < blackList.size(); i++)
			if (blackList.get(i).equalsIgnoreCase(p.getWorld().getName())) {
				returnValue = false;
				break;
			}

		return returnValue;
	}
}
