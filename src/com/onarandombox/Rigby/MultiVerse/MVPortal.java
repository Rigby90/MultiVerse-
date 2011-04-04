package com.onarandombox.Rigby.MultiVerse;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

public class MVPortal {
	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft");

	private Configuration config;
	private MultiVerse plugin;
	@SuppressWarnings("unused")
	private MVUtils utils;

	private String name;

	private String owner;

	private World world;
	private World destWorld = null;
	private String destLocation = null;

	public Vector min;
	public Vector max;
	public Vector center;

	private Double price = 0.0;
	private List<String> permissions;

	public MVPortal(String name, Configuration config, MultiVerse instance) {
		this.plugin = instance;
		this.utils = new MVUtils(plugin);
		this.config = config;

		this.name = name;
		this.owner = config.getString("portals." + name + ".owner", "");
		this.price = config.getDouble("portals." + name + ".price", 0.0);
		setupLocation();
		setDestLocation(this.config.getString("portals." + name
				+ ".destlocation", ""));
	}

	private void setupLocation() {
		String[] v = this.config.getString("portals." + name + ".location",
				"0,0,0:0,0,0").split(":");
		String[] min = v[0].split(",");
		String[] max = v[1].split(",");
		this.min = new Vector(Double.valueOf(min[0].toString()),
				Double.valueOf(min[1].toString()), Double.valueOf(min[2]
						.toString()));
		this.max = new Vector(Double.valueOf(max[0].toString()),
				Double.valueOf(max[1].toString()), Double.valueOf(max[2]
						.toString()));
		this.center = new Vector(
				(this.max.getBlockX() + this.min.getBlockX()) / 2,
				this.min.getBlockY(),
				(this.max.getBlockZ() + this.min.getBlockZ()) / 2);
		this.world = this.plugin.getServer().getWorld(
				config.getString("portals." + name + ".world", ""));
	}

	public void setOwner(String name) {
		this.owner = name;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setLocation(World world, Vector min, Vector max) {
		this.world = world;
		this.min = min;
		this.max = max;
	}

	public void setDestLocation(String l) {
		World w = null;
		if (!(l.contains(":"))) {
			return;
		}
		String[] s = l.split(":");
		if (!(s.length > 1)) {
			return;
		}

		if (s[0].toString().equalsIgnoreCase("W")) {
			w = this.plugin.getServer().getWorld(s[1].toString());
			if (w != null) {
				this.destWorld = w;
				this.destLocation = l;
			}
		}

		if (s[0].toString().equalsIgnoreCase("P")) {
			this.destWorld = null;
			this.destLocation = l;
		}
	}

	public String getDestLocation() {
		return this.destLocation;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public World getWorld() {
		return this.world;
	}

	public World getDestWorld() {
		return this.destWorld;
	}

	public Double getPrice() {
		return this.price;
	}

	public List<String> getPermissions() {
		return this.permissions;
	}

	public void save() {
		this.config.setProperty("portals." + this.name + ".world",
				this.world.getName());
		this.config.setProperty("portals." + this.name + ".location", this.min
				+ ":" + this.max);
		this.config.setProperty("portals." + this.name + ".price", this.price);
		this.config.setProperty("portals." + this.name + ".owner", this.owner);

		if (this.destLocation != null) {
			this.config.setProperty("portals." + this.name + ".destlocation",
					this.destLocation);
		} else {
			this.config.setProperty("portals." + this.name + ".destlocation",
					null);
		}

		this.config.save();
	}
}