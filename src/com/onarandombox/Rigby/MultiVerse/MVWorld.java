package com.onarandombox.Rigby.MultiVerse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.util.config.Configuration;

//import com.onarandombox.Rigby.util.Configuration;

public class MVWorld {

    private MultiVerse plugin;
    private MVUtils utils;
    private Configuration configuration;

    private World world;
    private String name;
    private Environment environment;
    private Location spawn;
    private Boolean mobs;
    private Boolean animals;
    private Boolean pvp;
    private Double price;
    private int ratio;
    private String alias = "";

    public List<String> blockBlacklist;
    public List<String> joinWhitelist;
    public List<String> joinBlacklist;
    public List<String> editWhitelist;
    public List<String> editBlacklist;
    public List<String> worldBlacklist;

    public MVWorld(World world, Configuration config, MultiVerse instance) {
        this.plugin = instance;
        this.utils = new MVUtils(plugin);
        this.world = world;
        this.name = world.getName();
        this.environment = world.getEnvironment();
        this.alias = config.getString("worlds." + name + ".alias", "");
        this.mobs = config.getBoolean("worlds." + name + ".mobs", true);
        this.animals = config.getBoolean("worlds." + name + ".animals", true);
        this.pvp = config.getBoolean("worlds." + name + ".pvp", true);
        // this.ratio = config.getInt("worlds." + name + ".ratio", 1);
        this.price = config.getDouble("worlds." + name + ".price", 0);
        this.configuration = config;

        this.joinWhitelist = setupPermissions("playerWhitelist");
        this.joinBlacklist = setupPermissions("playerBlacklist");
        this.editWhitelist = setupPermissions("editWhitelist");
        this.editBlacklist = setupPermissions("editBlacklist");
        this.worldBlacklist = setupPermissions("worldBlacklist");
        this.blockBlacklist = setupPermissions("blockBlacklist");

        setupSpawn();
    }

    public String getAlias() {
        return this.alias;
    }

    private List<String> setupPermissions(String permission) {
        List<String> list = new ArrayList<String>();

        String test = this.configuration.getString("worlds." + this.name + "." + permission, "").replace(']', ' ').replace('[', ' ').replaceAll(" ", "");

        if (!test.equals("")) {
            String[] temp = test.split(",");
            // log.info("Length - " + temp.length);
            if (temp.length > 0) {
                for (String element : temp) {
                    list.add(element.toString().trim());
                    // log.info("'" + temp[i].toString().trim() + "'");
                }
            }
        }
        return list;
    }

    private void setupSpawn() {
        String[] spawn = this.configuration.getString("worlds." + name + ".spawn", "").split(":");
        if (spawn.length != 5) {
            this.spawn = world.getSpawnLocation();
        } else {
            this.spawn = createLocationFromString(world, spawn[0], spawn[1], spawn[2], spawn[3], spawn[4]);
        }
    }

    private Location createLocationFromString(World world, String xStr, String yStr, String zStr, String yawStr, String pitchStr) {
        double x = Double.parseDouble(xStr);
        double y = Double.parseDouble(yStr);
        double z = Double.parseDouble(zStr);
        float yaw = Float.valueOf(yawStr).floatValue();
        float pitch = Float.valueOf(pitchStr).floatValue();

        return new Location(world, x, y, z, yaw, pitch);
    }

    public World getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Location getSpawnLocation() {
        return spawn;
    }

    public void setSpawnLocation(Location loc) {
        this.spawn = loc;
        saveAll();
    }

    public Boolean getMobSpawn() {
        return mobs;
    }

    public void setMobSpawn(Boolean mobs) {
        this.mobs = mobs;
        ((CraftWorld) this.world).getHandle().D = mobs;
        saveAll();
    }

    public void setAnimalSpawn(Boolean animals) {
        this.animals = animals;
        ((CraftWorld) this.world).getHandle().E = animals;
        // TODO: Cycle through all living entities and kill them.
        saveAll();
    }

    public Boolean getAnimalSpawn() {
        return this.animals;
    }

    public Boolean getPVP() {
        return this.pvp;
    }

    public void setPVP(Boolean pvp) {
        this.pvp = pvp;
    }

    public void setPrice(Double price) {
        this.price = price;
        saveAll();
    }

    public Double getPrice() {
        return this.price;
    }

    public int getRatio() {
        return this.ratio;
    }

    public void saveAll() {
        this.configuration.setProperty("worlds." + this.name + ".alias", this.alias);
        this.configuration.setProperty("worlds." + this.name + ".spawn", utils.locationToString(this.spawn));
        this.configuration.setProperty("worlds." + this.name + ".environment", this.environment.toString());
        this.configuration.setProperty("worlds." + this.name + ".mobs", this.mobs);
        this.configuration.setProperty("worlds." + this.name + ".animals", this.animals);
        this.configuration.setProperty("worlds." + this.name + ".pvp", this.pvp);
        this.configuration.setProperty("worlds." + this.name + ".price", this.price);
        // this.configuration.setProperty("worlds." + this.name + ".ratio",
        // this.ratio);

        StringBuilder whitelist = new StringBuilder();
        for (String value : joinWhitelist) {
            whitelist.append(value + ",");
        }
        this.configuration.setProperty("worlds." + this.name + ".playerWhitelist", whitelist.toString());

        StringBuilder blacklist = new StringBuilder();
        for (String value : joinBlacklist) {
            blacklist.append(value + ",");
        }
        this.configuration.setProperty("worlds." + this.name + ".playerBlacklist", blacklist.toString());

        StringBuilder worldblacklist = new StringBuilder();
        for (String value : worldBlacklist) {
            worldblacklist.append(value + ",");
        }
        this.configuration.setProperty("worlds." + this.name + ".worldBlacklist", worldblacklist.toString());

        this.configuration.save();
    }
}