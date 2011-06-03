package com.onarandombox.Rigby.MultiVerse;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MVCommands {
    private static final String PORTAL_NAME_REGEX = "[\\p{Alnum}[\\-]]*";
    public static final String WORLD_NAME_REGEX = "[\\p{Alnum}_[\\-]]*";
    private MultiVerse plugin;
    private MVUtils utils;

    private MVTeleport playerTeleporter;

    private final Logger log = Logger.getLogger("Minecraft");

    public MVCommands(MultiVerse instance) {
        this.plugin = instance;
        this.utils = new MVUtils(plugin);
        playerTeleporter = new MVTeleport(this.plugin);
    }

    /**
     * Add an existing World to the Setup
     * 
     * @param player
     * @param args
     */
    public void MVImport(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("Not enough parameters to Import an existing World");
            player.sendMessage(ChatColor.RED + "/mvimport WORLDNAME ENVIRONMENT - Import an existing World.");
            player.sendMessage(ChatColor.RED + "Example - /mvimport hellworld nether");
            return;
        }
        if (!new File(args[0].toString()).exists()) {
            player.sendMessage(ChatColor.RED + "World doesn't exist, stopping import!");
            return;
        }
        worldCreateImport(args[0].toString(), args[1].toString(), player, null);
    }

    /**
     * Create a new World and add it to the setup. allow for Generator Support.
     * 
     * @param player
     * @param args
     */
    public void MVCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Not enough parameters to create a new world");
            player.sendMessage(ChatColor.RED + "/mvcreate WORLDNAME ENVIRONMENT - Create a new World.");
            player.sendMessage(ChatColor.RED + "Example - /mvcreate hellworld nether");
            return;
        } else if(args.length > 3) {
        	player.sendMessage("Too many parameters to create a new world");
            player.sendMessage(ChatColor.RED + "/mvcreate WORLDNAME ENVIRONMENT - Create a new World.");
            player.sendMessage(ChatColor.RED + "Example - /mvcreate hellworld nether");
        }
        if (new File(args[0].toString()).exists()) {
            player.sendMessage(ChatColor.RED + "A Folder/World already exists with this name!");
            player.sendMessage(ChatColor.RED + "If you are confident it is a world you can import with /mvimport");
            return;
        }
        if(args.length > 2) {
        	worldCreateImport(args[0].toString(), args[1].toString(), player, args[2]);
        } else {
        	worldCreateImport(args[0].toString(), args[1].toString(), player, null);
        }
        
    }

    /**
     * Modify a current worlds settings, such as animals and mobs.
     * 
     * @param player
     * @param args
     */
    public void MVModify(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("/mvmodify {WORLDNAME} {OPTION}:{VALUE}");
            return;
        }
        String name = args[0];
        if (!name.matches(WORLD_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "World names can only be AlphaNumeric. Eg - 'world23_nether'");
            return;
        }
        if (!this.plugin.MVWorlds.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "There is no World by this name");
            return;
        }
        if (args.length == 1) {
            player.sendMessage("/mvmodify {WORLDNAME} {OPTION}:{VALUE}");
            return;
        }
        if (args.length == 2) {
            if (!args[1].contains(":")) {
                player.sendMessage("Not a valid option.");
                return;
            }
            String[] options = args[1].split(":");
            if (options[0].equalsIgnoreCase("MOBS")) {
                Boolean set = false;
                Boolean result = false;
                if (options[1].equalsIgnoreCase("TRUE") || options[1].equalsIgnoreCase("1") || options[1].equalsIgnoreCase("on") || options[1].equalsIgnoreCase("yes")) {
                    result = true;
                    set = true;
                }
                if (options[1].equalsIgnoreCase("FALSE") || options[1].equalsIgnoreCase("0") || options[1].equalsIgnoreCase("off") || options[1].equalsIgnoreCase("no")) {
                    result = false;
                    set = true;
                }
                if (set) {
                    player.sendMessage(ChatColor.RED + name + " - Mobs set to " + result.toString());
                    this.plugin.MVWorlds.get(name).setMobSpawn(result);
                    this.plugin.MVWorlds.get(name).saveAll();
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + name + "Invalid Setting");
                    return;
                }
            }
            if (options[0].equalsIgnoreCase("ANIMALS")) {
                Boolean set = false;
                Boolean result = false;
                if (options[1].equalsIgnoreCase("TRUE") || options[1].equalsIgnoreCase("1") || options[1].equalsIgnoreCase("on") || options[1].equalsIgnoreCase("yes")) {
                    result = true;
                    set = true;
                }
                if (options[1].equalsIgnoreCase("FALSE") || options[1].equalsIgnoreCase("0") || options[1].equalsIgnoreCase("off") || options[1].equalsIgnoreCase("no")) {
                    result = false;
                    set = true;
                }
                if (set) {
                    player.sendMessage(ChatColor.RED + name + " - Animals set to " + result.toString());
                    this.plugin.MVWorlds.get(name).setAnimalSpawn(result);
                    this.plugin.MVWorlds.get(name).saveAll();
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + name + "Invalid Setting");
                    return;
                }
            }
            if (options[0].equalsIgnoreCase("PVP")) {
                Boolean set = false;
                Boolean result = false;
                if (options[1].equalsIgnoreCase("TRUE") || options[1].equalsIgnoreCase("1") || options[1].equalsIgnoreCase("on") || options[1].equalsIgnoreCase("yes")) {
                    result = true;
                    set = true;
                }
                if (options[1].equalsIgnoreCase("FALSE") || options[1].equalsIgnoreCase("0") || options[1].equalsIgnoreCase("off") || options[1].equalsIgnoreCase("no")) {
                    result = false;
                    set = true;
                }
                if (set) {
                    player.sendMessage(ChatColor.RED + name + " - PVP set to " + result.toString());
                    this.plugin.MVWorlds.get(name).setPVP(result);
                    this.plugin.MVWorlds.get(name).saveAll();
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + name + "Invalid Setting");
                    return;
                }
            }
            player.sendMessage(ChatColor.RED + "Invalid Option");
        }
    }

    /**
     * Function which is called by MVCreate and MVImport in order to add the Worlds to the Server
     * and to MultiVerse configs.
     * 
     * @param name
     * @param env
     * @param player
     */
    private void worldCreateImport(String name, String env, Player player, String seed) {
        env = env.toUpperCase();
        Environment environment = null;
        try {
            environment = Environment.valueOf(env);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Environment type does not exist!");
            MVEnvironments(player);
            return;
        }

        if (environment == null) {
            player.sendMessage(ChatColor.RED + "Environment type does not exist!");
            MVEnvironments(player);
            return;
        }

        if (!name.matches(WORLD_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "World names can only be AlphaNumeric. Eg - 'world23_nether'");
            return;
        }
        if (!this.plugin.MVWorlds.containsKey(name)) {
            World world;
            if (this.plugin.getServer().getWorld(name) == null) {
            	if(seed == null) {
					this.plugin.getServer().broadcastMessage(ChatColor.RED + "Attempting to create a new World");
					
					world = this.plugin.getServer().createWorld(name, environment);
            	} else {
            		this.plugin.getServer().broadcastMessage(ChatColor.RED + "Attempting to create a new World with seed " + seed);
            		Long seedLong;
            		try {
                        seedLong = Long.parseLong(seed);
                    } catch (NumberFormatException e) {
                        seedLong = (long) seed.hashCode();
                    }
                    world = this.plugin.getServer().createWorld(name, environment, seedLong);
            	}
                
                log.info("[MultiVerse] " + name + " - World Created as - " + env.toString());
            } else {
                world = this.plugin.getServer().getWorld(name);
                log.info("[MultiVerse] " + name + " - World Imported as - " + env.toString());

            }
            this.plugin.MVWorlds.put(world.getName(), new MVWorld(world, this.plugin.configWorlds, this.plugin));
            this.plugin.MVWorlds.get(world.getName()).saveAll();
            this.plugin.getServer().broadcastMessage(ChatColor.GREEN + "Complete");
        } else {
            player.sendMessage(ChatColor.RED + "A World with that name already exists.");
        }
    }

    /**
     * Remove the specified World from MultiVerse configuration.
     * 
     * @param player
     * @param args
     */
    public void MVRemove(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("Not enough parameters to Remove a loaded World");
            player.sendMessage(ChatColor.RED + "/mvremove WORLDNAME - Remove a loaded World.");
            player.sendMessage(ChatColor.RED + "Example - /mvremove hellworld");
            return;
        }
        String name = args[0].toString();
        if (!name.matches(WORLD_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "World names can only be AlphaNumeric. Eg - 'world23_nether'");
            return;
        }
        if (!this.plugin.MVWorlds.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "World doesn't exist!");
            return;
        } else {
            this.plugin.MVWorlds.remove(name);
            this.plugin.configWorlds.removeProperty("worlds." + name);
            this.plugin.configWorlds.save();
            player.sendMessage(ChatColor.RED + name + " has been removed from MultiVerse.");
            player.sendMessage(ChatColor.RED + "However it won't take effect till server restart.");
        }
    }

    /**
     * Tells players what the available environments are and how to spell them.
     * 
     * @param player
     */
    public void MVEnvironments(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Valid Environments are:");
        player.sendMessage(ChatColor.GREEN + "NORMAL");
        player.sendMessage(ChatColor.RED + "NETHER");
        player.sendMessage(ChatColor.AQUA + "SKYLANDS");
    }

    /**
     * Create a new portal from the coordinates given. TODO: Permissions... and everything else...
     * 
     * @param player
     * @param split
     */
    public void MVPCreate(Player player, String[] split) {
        if (split.length == 0) {
            player.sendMessage("/mvpc {PORTALNAME} - Create a Portal without a destination.");
            player.sendMessage("/mvpc {PORTALNAME} [P:PORTALNAME] - Create a portal a Portal Destination");
            player.sendMessage("/mvpc {PORTALNAME} [W:WORLDNAME] - Create a portal with a World Destination");
            return;
        }
        String name = split[0];
        if (!name.matches(PORTAL_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "Portal names can only be AlphaNumeric. Eg - 'world23'");
            return;
        }
        if (name.equalsIgnoreCase("null")) {
            player.sendMessage("Cannot name a Portal NULL");
            return;
        }
        MVPlayerSession ps = this.plugin.playerSessions.get(player.getName());
        if (ps.getLocation1() == null || ps.getLocation2() == null) {
            player.sendMessage("You have not set your coordinates yet");
            return;
        }
        if (this.plugin.MVPortals.containsKey(name)) {
            player.sendMessage("A Portal already exists with this name");
            return;
        }
        // Check for Real Portals for Legit servers.
        if (this.plugin.configMV.getBoolean("realportals", false)) {
            int check = this.utils.checkRealPortal(this.plugin.playerSessions.get(player.getName()));
            if (check != 6) {
                player.sendMessage("This server requires standard Nether Portals made of obsidian and portal material.");
                return;
            }
        }

        Vector[] v = this.utils.getMinMax(ps);
        Boolean existingPortal = utils.isPortal(player.getWorld(), v[0], v[1]);

        if (existingPortal) {
            player.sendMessage("A Portal already exists within your selection");
            return;
        }

        ps.setSelectedPortal(name);
        this.plugin.MVPortals.put(name, new MVPortal(name, this.plugin.configPortals, this.plugin));

        this.plugin.MVPortals.get(name).setLocation(player.getWorld(), v[0], v[1]);
        this.plugin.MVPortals.get(name).setOwner(player.getName());

        // log.info("Why - " + split.length);

        if (split.length == 1) {
            player.sendMessage("Portal created, now you need to set a destination");
        }

        if (split.length == 2) {
            String[] destName = split[1].split(":");
            if (destName[0].equalsIgnoreCase("W")) {
                World w = this.plugin.getServer().getWorld(destName[1].toString());
                // TODO: Permissions...
                if (w != null) {
                    this.plugin.MVPortals.get(name).setDestLocation("w:" + w.getName() + ":spawn");
                    player.sendMessage("Portal created.");
                } else {
                    player.sendMessage("No World by this name");
                }
            }
            if (destName[0].equalsIgnoreCase("P")) {
                if (this.plugin.MVPortals.containsKey(name)) {
                    // TODO: Add a form of portal restriction...
                    // "I don't want people to dest my portal as their destination".
                    this.plugin.MVPortals.get(name).setDestLocation("p:" + destName[1].toString());
                    player.sendMessage("Portal created.");
                } else {
                    player.sendMessage("There is no portal by this name");
                }
            }
        }
        this.plugin.MVPortals.get(name).save();
    }

    /**
     * Remove an existing portal from configuration. TODO: Admin Permissions
     * 
     * @param player
     * @param split
     */
    public void MVPRemove(Player player, String[] split) {
        if (split.length == 0) {
            player.sendMessage("Please specify a name for the Portal");
            return;
        }
        String name = split[0];
        if (!name.matches(PORTAL_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "Portal names can only be AlphaNumeric. Eg - 'portal23'");
            return;
        }
        if (name.length() == 0) {
            player.sendMessage(ChatColor.RED + "Please type the name of the Portal you wish to remove.");
            return;
        }
        if (!this.plugin.MVPortals.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "No Portal by this name");
            return;
        }
        MVPortal p = this.plugin.MVPortals.get(name);
        if (player.getName().equalsIgnoreCase(p.getOwner()) || MultiVerse.Permissions.has(player, "multiverse.portal.override")) {
            this.plugin.MVPortals.remove(name);
            this.plugin.configPortals.removeProperty("portals." + name);
            this.plugin.configPortals.save();
            player.sendMessage(ChatColor.RED + "Portal Deleted!");
        }
    }

    /**
     * Rename an existing portal.
     * 
     * @param player
     * @param split
     */
    public void MVPRename(Player player, String[] split) {
        if (split.length != 2) {
            player.sendMessage("/mvprename {OLDNAME} {NEWNAME}");
            return;
        }
        String oldname = split[0];
        String newname = split[1];
        if (!oldname.matches(PORTAL_NAME_REGEX) || !newname.matches(PORTAL_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "Portal names can only be AlphaNumeric. Eg - 'portal23'");
            return;
        }
        if (oldname.length() == 0 || newname.length() == 0) {
            player.sendMessage("/mvprename {OLDNAME} {NEWNAME}");
            return;
        }
        if (!this.plugin.MVPortals.containsKey(oldname)) {
            player.sendMessage(ChatColor.RED + "No Portal by this name");
            return;
        }
        MVPortal p = this.plugin.MVPortals.get(oldname);
        if (player.getName().equalsIgnoreCase(p.getOwner()) || MultiVerse.Permissions.has(player, "multiverse.portal.override")) {
            this.plugin.MVPortals.remove(oldname);
            this.plugin.configPortals.removeProperty("portals." + oldname);
            this.plugin.configPortals.save();

            this.plugin.MVPortals.put(newname, p);
            this.plugin.MVPortals.get(newname).setName(newname);
            this.plugin.MVPortals.get(newname).save();
            player.sendMessage(ChatColor.RED + oldname + " - renamed to - " + newname);
        }
    }

    /**
     * Set a Portal destination.
     * 
     * @param player
     * @param split
     */
    public void MVPDestination(Player player, String[] split) {
        if (this.plugin.playerSessions.get(player.getName()).getSelectedPortal() == "") {
            player.sendMessage("You need to select a portal before assigning a destination");
            return;
        }
        String name = this.plugin.playerSessions.get(player.getName()).getSelectedPortal();
        if (name == null) {
            player.sendMessage("Please select a portal with '/mvps {PORTALNAME}'.");
            return;
        }
        if (!this.plugin.MVPortals.get(name).getOwner().equalsIgnoreCase(player.getName()) && !MultiVerse.Permissions.has(player, "multiverse.portal.override")) {
            player.sendMessage("You cannot set/clear the destination of a portal that does not belong to you");
            return;
        }
        if (split.length == 0) {
            player.sendMessage("Please choose a destination.");
            return;
        }
        if (split[0].equalsIgnoreCase("clear")) {
            this.plugin.MVPortals.get(name).setDestLocation(null);
            player.sendMessage("Portal destinations cleared");
        }
        if (split[0].equalsIgnoreCase("here")) {
            this.plugin.MVPortals.get(name).setDestLocation("w:" + player.getWorld().getName() + ":" + utils.locationToString(player.getLocation()));
            player.sendMessage("Portal location set to your position");
        }
        String[] destName = split[0].split(":");
        if (destName[0].equalsIgnoreCase("W")) {
            World w = this.plugin.getServer().getWorld(destName[1].toString());
            // TODO: Permissions...
            if (w != null) {
                MVWorld ws = this.plugin.MVWorlds.get(w.getName());
                this.plugin.MVPortals.get(name).setDestLocation("w:" + ws.getName() + ":spawn");
                player.sendMessage("Set destination to the worlds spawn. " + destName[1]);
            } else {
                player.sendMessage("No World by this name");
            }
        }
        if (destName[0].equalsIgnoreCase("P")) {
            if (this.plugin.MVPortals.containsKey(name)) {
                // TODO: Add a form of portal restriction...
                // "I don't want people to set my portal as their destination".
                this.plugin.MVPortals.get(name).setDestLocation("p:" + destName[1].toString());
                player.sendMessage("Set destination to portal. " + destName[1]);
            } else {
                player.sendMessage("There is no portal by this name");
            }
        }
        this.plugin.MVPortals.get(name).save();
    }

    /**
     * Set the users selected Portal
     * 
     * @param player
     * @param split
     */
    public void MVPSelect(Player player, String[] split) {
        if (split.length == 0) {
            player.sendMessage("Please specify a portal to select");
            return;
        }
        String name = split[0].toString();
        if (!name.matches(PORTAL_NAME_REGEX)) {
            player.sendMessage(ChatColor.RED + "Portal names can only be AlphaNumeric. Eg - 'portal23'");
            return;
        }
        if (this.plugin.MVPortals.containsKey(split[0].toString())) {
            this.plugin.playerSessions.get(player.getName()).setSelectedPortal(name);
            player.sendMessage(name + " - Portal Selected");
        } else {
            player.sendMessage("No portal with this name");
        }
    }

    /**
     * Display all the portals to the player. TODO: Permissions.
     * 
     * @param player
     * @param split
     */
    public void MVPList(Player player, String[] split) {
        player.sendMessage("Portals on this server that you can use -");

        for (String key : this.plugin.MVPortals.keySet()) {
            player.sendMessage(this.plugin.MVPortals.get(key).getName());
        }
    }

    /**
     * Build a Portal near the player in the direction they are facing.
     * 
     * @param player
     * @param split
     */
    public void MVPBuild(Player player, String[] split) {
        utils.buildPortal(player, 0);
    }

    /**
     * Print the current loaded Worlds to the player
     * 
     * @param player
     */
    public void MVList(Player player) {
        List<World> worlds = this.plugin.getServer().getWorlds();
        player.sendMessage("Worlds running on this Server -");
        for (int i = 0; i < worlds.size(); i++) {
            ChatColor color;
            Environment env = worlds.get(i).getEnvironment();
            if (env == Environment.NETHER) {
                color = ChatColor.RED;
            } else if (env == Environment.NORMAL) {
                color = ChatColor.GREEN;
            } else if (env == Environment.SKYLANDS) {
                color = ChatColor.AQUA;
            } else {
                color = ChatColor.WHITE;
            }

            player.sendMessage(color + worlds.get(i).getName());
        }
    }

    /**
     * Set the Worlds spawn location to the players current location.
     * 
     * @param player
     * @param args
     */
    public void MVSetSpawn(Player player, String[] args) {
        World w = player.getWorld();
        this.plugin.MVWorlds.get(w.getName()).setSpawnLocation(player.getLocation());
        player.sendMessage(w.getName() + " - Spawn Set");
    }

    /**
     * Teleport the player to the Worlds assigned spawn point.
     * 
     * @param player
     * @param split
     */
    public void MVSpawn(Player player, String[] split) {
        World world = player.getWorld();
        Location l = this.plugin.MVWorlds.get(world.getName()).getSpawnLocation();
        l = playerTeleporter.getDestination(l, player);
        player.teleport(l);
    }

    /**
     * Teleport the player to the World or Portal they specify. TODO: Permissions, can a user
     * teleport to that world, blacklists/whitelist
     * 
     * @param player
     * @param args
     */
    public void MVTP(Player player, String[] args) {
        Location l = null;
        if (args.length == 1 && args[0].length() > 0) {
            World w = null;
            if (args[0].contains(":")) {
                String[] s = args[0].split(":");
                if (s.length == 2) {
                    if (s[0].equalsIgnoreCase("P")) {
                        if (!s[1].matches(PORTAL_NAME_REGEX)) {
                            player.sendMessage(ChatColor.RED + "Portal names can only be AlphaNumeric. Eg - 'world23'");
                            return;
                        }
                        if (this.plugin.MVPortals.containsKey(s[1].toString())) {
                            MVPortal dest = this.plugin.MVPortals.get(s[1]);
                            w = dest.getWorld();
                            Vector v = dest.center;
                            l = new Location(w, v.getX(), v.getY(), v.getZ(), (float) 0.0, (float) 0.0);
                            l = playerTeleporter.getPortalDestination(l, player);
                        }
                    }
                    if (s[0].equalsIgnoreCase("W")) {
                        if (!s[1].matches(WORLD_NAME_REGEX)) {
                            player.sendMessage(ChatColor.RED + "World names can only be AlphaNumeric. Eg - 'world23_nether'");
                            return;
                        }
                        w = this.plugin.getServer().getWorld(s[1].toString());
                        l = this.plugin.MVWorlds.get(w.getName()).getSpawnLocation();
                        l = playerTeleporter.getDestination(l, player);
                    }
                }
            } else {
                if (!args[0].matches(WORLD_NAME_REGEX)) {
                    player.sendMessage(ChatColor.RED + "World names can only be AlphaNumeric. Eg - 'world23'");
                    return;
                }
                w = this.plugin.getServer().getWorld(args[0].toString());
                if (w != null) {
                    l = this.plugin.MVWorlds.get(w.getName()).getSpawnLocation();
                    l = playerTeleporter.getDestination(l, player);
                }
            }
            if (l == null || l.getWorld() == null) {
                player.sendMessage(ChatColor.RED + "World/Portal with that name doesn't exist");
                return;
            }
        } else {
            player.sendMessage(ChatColor.RED + "/mvtp {WORLDNAME} - Teleports you to the specified World.");
            player.sendMessage(ChatColor.RED + "/mvtp w:{WORLDNAME} - Teleports you to the specified World.");
            player.sendMessage(ChatColor.RED + "/mvtp p:{PORTALNAME} - Teleports you to the specified Portal.");
            return;
        }
        if (l != null && l.getWorld() != null) {
            player.teleport(l);
        }
    }

    /**
     * Teleport a player to a world.
     * 
     * @param player
     * @param split
     */
    public void MVTPT(Player player, String[] args) {
        playerTeleporter.getPortalDestination(player.getLocation(), player);
        return;
    }

    /**
     * Output help to the player. TODO: Need to implement permissions, only display commands the
     * player can actually use.
     * 
     * @param player
     */
    public void MVHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "[MultiVerse] Commands -");
        player.sendMessage(ChatColor.RED + "/mv create WORLDNAME ENVIRONMENT - Create a new World.");
        player.sendMessage("Example - /mv create hellworld nether");
        player.sendMessage(ChatColor.RED + "/mvtp WORLDNAME - Teleport to the specified World.");
        player.sendMessage("Example - /mvtp hellworld");
        player.sendMessage(ChatColor.RED + "/mvlist - Output a list of all the loaded Worlds.");
        player.sendMessage(ChatColor.RED + "/mvsetspawn - Set current Worlds spawn to Player Location");
        player.sendMessage(ChatColor.RED + "/mvspawn - Teleport to the Spawn of the current World");
    }

    /**
     * Output Coordinates and World
     * 
     * @param player
     */
    public void MVCoords(Player player) {
        String w = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        player.sendMessage("World - " + w);
        player.sendMessage("Coords - X: " + x + " Y: " + y + " Z: " + z);
    }
}
