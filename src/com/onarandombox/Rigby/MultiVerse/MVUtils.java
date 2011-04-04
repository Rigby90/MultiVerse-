package com.onarandombox.Rigby.MultiVerse;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MVUtils {

	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft");

	private MultiVerse plugin;

	public MVUtils(MultiVerse instance) {
		this.plugin = instance;
	}

	/**
	 * Turn a Location into a storeable string.
	 * 
	 * @param location
	 * @return
	 */
	public String locationToString(Location location) {
		StringBuilder test = new StringBuilder();
		test.append(location.getBlockX() + ":");
		test.append(location.getBlockY() + ":");
		test.append(location.getBlockZ() + ":");
		test.append(location.getYaw() + ":");
		test.append(location.getPitch());
		return test.toString();
	}

	/**
	 * Turn a String into a location.
	 * 
	 * @param w
	 * @param location
	 * @return
	 */
	public Location stringToLocation(World w, String location) {
		String[] l = location.split(":");
		double x = Double.valueOf(l[0]);
		double y = Double.valueOf(l[1]);
		double z = Double.valueOf(l[2]);
		float yaw = Float.valueOf(l[3]);
		float pitch = Float.valueOf(l[4]);
		Location loc = new Location(w, x, y, z, yaw, pitch);
		return loc;
	}

	/**
	 * Get the minimum and maximum point of a selected cuboid.
	 * 
	 * @param coords
	 * @return
	 */
	public Vector[] getMinMax(MVPlayerSession coords) {
		Vector[] v = new Vector[2];
		Location l1 = coords.getLocation1();
		Location l2 = coords.getLocation2();
		int minX = Math.min(l1.getBlockX(), l2.getBlockX());
		int minY = Math.min(l1.getBlockY(), l2.getBlockY());
		int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		int maxY = Math.max(l1.getBlockY(), l2.getBlockY());
		int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());

		v[0] = new Vector(minX, minY, minZ);
		v[1] = new Vector(maxX, maxY, maxZ);

		return v;
	}

	/**
	 * For servers that want to play with legit portals, we need to check that 6
	 * portal pieces exist.
	 * 
	 * @param coords
	 * @return
	 */
	public int checkRealPortal(MVPlayerSession coords) {
		Location l1 = coords.getLocation1();
		Location l2 = coords.getLocation2();

		World w = l1.getWorld();
		int minX = Math.min(l1.getBlockX(), l2.getBlockX());
		int minY = Math.min(l1.getBlockY(), l2.getBlockY());
		int minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		int maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		int maxY = Math.max(l1.getBlockY(), l2.getBlockY());
		int maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());

		int count = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (w.getBlockAt(x, y, z).getTypeId() == 90) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * Build a Portal at the Location, ignore Direction for Now.
	 * 
	 * @param l
	 */
	public void buildPortal(Location l) {
		buildPortalNorthSouth(l, -1);
		return;
	}

	/**
	 * Build a Portal, work out the Direction first then pass it on to the
	 * relevant function.
	 * 
	 * @param player
	 * @param offset
	 */
	public void buildPortal(Player player, int offset) {
		String dir = getDirection(player);
		if (dir.equalsIgnoreCase("N")) {
			buildPortalNorthSouth(player.getLocation(), -1);
			return;
		}
		if (dir.equalsIgnoreCase("S")) {
			buildPortalNorthSouth(player.getLocation(), 1);
			return;
		}
		if (dir.equalsIgnoreCase("E")) {
			buildPortalEastWest(player.getLocation(), -1);
			return;
		}
		if (dir.equalsIgnoreCase("W")) {
			buildPortalEastWest(player.getLocation(), 1);
			return;
		}
		player.sendMessage("Be more specific with the direction of the portal.");
	}

	/**
	 * Build a portal facing either East or West
	 * 
	 * @param player
	 * @param offset
	 */
	public void buildPortalEastWest(Location l, int offset) {
		// World world = player.getWorld();
		World world = l.getWorld();
		// Location l = player.getLocation();
		int x = l.getBlockX();
		int y = l.getBlockY() + 1;
		int z;

		z = l.getBlockZ() + offset;

		world.getBlockAt(x + 2, y - 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 2, y, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 2, y + 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 2, y + 2, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 2, y + 3, z).setType(Material.OBSIDIAN);

		world.getBlockAt(x - 1, y - 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x - 1, y, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x - 1, y + 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x - 1, y + 2, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x - 1, y + 3, z).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y - 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 1, y - 1, z).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y + 3, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x + 1, y + 3, z).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y + 1, z).setType(Material.AIR);
		world.getBlockAt(x, y + 2, z).setType(Material.AIR);
		world.getBlockAt(x + 1, y + 1, z).setType(Material.AIR);
		world.getBlockAt(x + 1, y + 2, z).setType(Material.AIR);
		world.getBlockAt(x, y, z).setType(Material.AIR);
		world.getBlockAt(x + 1, y, z).setType(Material.FIRE);
	}

	/**
	 * Build a portal facing either North or South
	 * 
	 * @param player
	 * @param offset
	 */
	public void buildPortalNorthSouth(Location l, int offset) {
		World world = l.getWorld();
		// Location l = player.getLocation();
		int x;
		int y = l.getBlockY() + 1;
		int z = l.getBlockZ();

		x = l.getBlockX() + offset;

		world.getBlockAt(x, y - 1, z + 2).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y, z + 2).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 1, z + 2).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 2, z + 2).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 3, z + 2).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y - 1, z - 1).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y, z - 1).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 1, z - 1).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 2, z - 1).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 3, z - 1).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y - 1, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y - 1, z + 1).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y + 3, z).setType(Material.OBSIDIAN);
		world.getBlockAt(x, y + 3, z + 1).setType(Material.OBSIDIAN);

		world.getBlockAt(x, y + 1, z).setType(Material.AIR);
		world.getBlockAt(x, y + 2, z).setType(Material.AIR);
		world.getBlockAt(x, y + 1, z + 1).setType(Material.AIR);
		world.getBlockAt(x, y + 2, z + 1).setType(Material.AIR);
		world.getBlockAt(x, y, z).setType(Material.AIR);
		world.getBlockAt(x, y, z + 1).setType(Material.FIRE);
	}

	/**
	 * Take the players location and returns direction in the form of - N S E W
	 * - NE NW SE SW
	 * 
	 * @param player
	 * @return
	 */
	public String getDirection(Player player) {
		int r = (int) Math.abs((player.getLocation().getYaw() - 90) % 360);
		String dir;
		if (r < 23)
			dir = "N";
		else if (r < 68)
			dir = "NE";
		else if (r < 113)
			dir = "E";
		else if (r < 158)
			dir = "SE";
		else if (r < 203)
			dir = "S";
		else if (r < 248)
			dir = "SW";
		else if (r < 293)
			dir = "W";
		else if (r < 338)
			dir = "NW";
		else
			dir = "N";

		return dir;
	}

	/**
	 * Check to see if the block we are smacking about is actually part of a
	 * portal.
	 * 
	 * @param location
	 * @return
	 */
	public String isPortal(Location l) {
		Set<String> portalKeys = this.plugin.MVPortals.keySet();
		if (portalKeys == null) {
			return null;
		}
		for (String key : portalKeys) {
			Vector min = this.plugin.MVPortals.get(key).min;
			Vector max = this.plugin.MVPortals.get(key).max;
			World wcheck = this.plugin.MVPortals.get(key).getWorld();
			if (wcheck != null) {
				String w = wcheck.getName();
				if (l.getWorld().getName().equalsIgnoreCase(w)) {
					for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
						for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
							for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
								if (l.getBlockX() == x && l.getBlockY() == y
										&& l.getBlockZ() == z) {
									return this.plugin.MVPortals.get(key)
											.getName();
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check to see if the Location contains a Portal
	 * 
	 * @param w
	 * @param min
	 * @param max
	 * @return
	 */
	public boolean isPortal(World w, Vector min, Vector max) {
		Set<String> portalKeys = this.plugin.MVPortals.keySet();
		if (portalKeys == null) {
			return false;
		}
		for (String key : portalKeys) {
			Vector min1 = this.plugin.MVPortals.get(key).min;
			Vector max1 = this.plugin.MVPortals.get(key).max;
			World l = this.plugin.MVPortals.get(key).getWorld();
			List<String> oldBlocks = new ArrayList<String>();
			if (w.getName().equalsIgnoreCase(l.getName())) {
				for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
					for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
						for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
							oldBlocks.add(x + ":" + y + ":" + z);
						}
					}
				}
				for (int x = min1.getBlockX(); x <= max1.getBlockX(); x++) {
					for (int y = min1.getBlockY(); y <= max1.getBlockY(); y++) {
						for (int z = min1.getBlockZ(); z <= max1.getBlockZ(); z++) {
							if (oldBlocks.contains(x + ":" + y + ":" + z)) {
								return true;
							}
						}
					}
				}

			}
		}
		return false;
	}
}