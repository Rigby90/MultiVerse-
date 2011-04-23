package com.onarandombox.Rigby.MultiVerse;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

@SuppressWarnings("unused")
public class MultiVerse extends JavaPlugin {
    /**
     * Variable to contain the CraftBukkit version which this was made for and
     * servers should not use anything less than this.
     */
    private final int CBVer = 320;
    /**
     * Variables to contain checks on the external plugins, so far this is just
     * Permissions and iConomy. Permissions is REQUIRED.
     */
    public static PermissionHandler Permissions;
    public static boolean useiConomy = false;
    /**
     * Setup the Logger, also set a public variable which contains the prefix
     * for all log messages, this allows for easy change.
     */
    private final Logger log = Logger.getLogger("Minecraft");
    public final String logPrefix = "[MultiVerse] ";
    /**
     * Setup all the Configuration varibles... first 3 are actual config files
     * to read write to, 4 one is the process we run to setup the initial files.
     */
    public Configuration configMV;
    public Configuration configWorlds;
    public Configuration configPortals;
    private MVConfiguration confSetup;
    /**
     * Setup our commands and utilities which we'll use.
     */
    public MVUtils utils = new MVUtils(this);
    private MVCommands commandsMV = new MVCommands(this);
    /**
     * Setup the Update Checker which we'll start onEnable, no point starting it
     * any earlier incase we disable before we actually enable.
     */
    public MVUpdateCheck updateCheck = null;
    /**
     * HashMaps to contain the current Worlds and Portals loaded into
     * MultiVerse.
     */
    public HashMap<String, MVWorld> MVWorlds = new HashMap<String, MVWorld>();
    public HashMap<String, MVPortal> MVPortals = new HashMap<String, MVPortal>();
    /**
     * HashMap to contain all player session related stuff, eg what coordinates
     * they have selected or what portal they have selected.
     */
    public HashMap<String, MVPlayerSession> playerSessions = new HashMap<String, MVPlayerSession>();
    /**
     * Setup the block/player/entity listener.
     */
    private MVPlayerListener playerListener;
    private MVBlockListener blockListener;
    private MVEntityListener entityListener;
    private MVPluginListener pluginListener;

    @Override
    public void onLoad() {
        getDataFolder().mkdirs();

        configMV = new Configuration(new File(this.getDataFolder(), "MultiVerse.yml"));
        configWorlds = new Configuration(new File(this.getDataFolder(), "Worlds.yml"));
        configPortals = new Configuration(new File(this.getDataFolder(), "Portals.yml"));
        confSetup = new MVConfiguration(this.getDataFolder(), this);

        /**
         * First we run our 'confSetup' class to check if the Configuration
         * files exist. If either of them is missing it will create it with a
         * empty template.
         */
        confSetup.setupConfigs();
        /**
         * Load the main MultiVerse properties file.
         */
        configMV.load();
        configWorlds.load();
        loadWorlds();
    }

    @Override
    public void onEnable() {
        pluginListener = new MVPluginListener(this);

        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);

        if (!checkPermissions()) {
            log.info(logPrefix + "Commands are disabled unless Permissions is found.");
        } else {
            log.info(logPrefix + "Permissions Found");
        }

        playerListener = new MVPlayerListener(this, configMV);
        blockListener = new MVBlockListener(this, configMV);
        entityListener = new MVEntityListener(this);

        /**
         * Output a little snipper to state that the Plugin is now enabled.
         */
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled");
        /**
         * CraftBukkit Version checking -- Removed for now as the way they
         * display the CB Build has changed.
         */
        /*
         * try // Example Output -
         * "git-Bukkit-0.0.0-382-g026d9db-b297 (MC: 1.2_01)" string[] verString
         * = this.getServer().getVersion().split("-"); int verNumber =
         * Integer.valueOf
         * ((verString[verString.length-1].split(" ")[0].replace("b", "")));
         * if(!(verNumber>=CBVer)){ log.log(Level.WARNING,
         * "[MultiVerse] requires CraftBukkit build " + CBVer +
         * "+, disabling MultiVerse to protect your server.");
         * this.getServer().getPluginManager().disablePlugin(this); return; } }
         * catch (Exception ex){ log.log(Level.WARNING,
         * "[MultiVerse] requires CraftBukkit build " + CBVer +
         * "+. Proceed with CAUTION!"); }
         */

        /**
         * If enabled start up the update checker, every now and then this will
         * check the version file on the server to see if it's currently running
         * the latest version.
         */
        if (configMV.getBoolean("checkupdates", true)) {
            // updateCheck = new MVUpdateCheck(this);
        }
        /**
         * Check whether iConomy is enabled, then check whether they want
         * players to be charged for using Portals.
         */
        if (checkiConomy()) {
            useiConomy = configMV.getBoolean("iconomy", false);
        }
        /**
         * Since we have everything we need to run the plugin we can now load
         * the config files.
         */
        configPortals.load();
        /**
         * Now we run the functions to place each world and portal into our
         * HashMap with our Custom Class.
         */
        loadPortals();
        /**
         * Setup all the events which we need to listen for.
         */
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Low, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.High, this);

        reloadPlayerSessions();
    }

    public void reloadPlayerSessions() {
        Player[] p = this.getServer().getOnlinePlayers();
        for (Player element : p) {
            this.playerSessions.put(element.getName(), new MVPlayerSession(element, this.configMV));
        }
    }

    /**
     * When a command is run this Function will process it and perform the
     * appropriate action.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] split = args;
        String commandName = command.getName().toLowerCase();

        if (!(sender instanceof Player)) {
            log.info(logPrefix + " This command needs to be run as a Player.");
            return true;
        }
        Player player = (Player) sender;

        if (MultiVerse.Permissions == null) {
            sender.sendMessage("Permissions is not Enabled, or hasn't been found.");
            return true;
        }

        if (commandName.equalsIgnoreCase("mvcreate")) {
            if (!Permissions.has(player, "multiverse.world.create")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVCreate(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvimport")) {
            if (!Permissions.has(player, "multiverse.world.import")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVImport(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvremove")) {
            if (!Permissions.has(player, "multiverse.world.remove")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVRemove(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvmodify")) {
            if (!Permissions.has(player, "multiverse.world.modify")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVModify(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvtp")) {
            if (!Permissions.has(player, "multiverse.tp")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVTP(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvtpt")) {
            if (!Permissions.has(player, "multiverse.tpt")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVTPT(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvlist")) {
            if (!Permissions.has(player, "multiverse.list")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVList(player);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvsetspawn")) {
            if (!Permissions.has(player, "multiverse.world.setspawn")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVSetSpawn(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvspawn")) {
            if (!Permissions.has(player, "multiverse.world.spawn")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVSpawn(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvpc") || commandName.equalsIgnoreCase("mvpcreate")) {
            if (!Permissions.has(player, "multiverse.portal.create")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPCreate(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvpd")) {
            if (!Permissions.has(player, "multiverse.portal.destination")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPDestination(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvps") || commandName.equalsIgnoreCase("mvpselect")) {
            if (!Permissions.has(player, "multiverse.portal.select")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPSelect(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvpr") || commandName.equalsIgnoreCase("mvpremove")) {
            if (!Permissions.has(player, "multiverse.portal.remove")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPRemove(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvprename")) {
            if (!Permissions.has(player, "multiverse.portal.rename")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPRename(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvplist")) {
            if (!Permissions.has(player, "multiverse.portal.list")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPList(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvpbuild")) {
            if (!Permissions.has(player, "multiverse.portal.build")) {
                player.sendMessage("You do not have the rights to access this command");
                return true;
            }
            commandsMV.MVPBuild(player, split);
            return true;
        }

        if (commandName.equalsIgnoreCase("mvcoord")) {
            commandsMV.MVCoords(player);
            return true;
        }

        commandsMV.MVHelp(player);
        return true;
    }

    /**
     * This section is run once when the Plugin is enabled, this will load all
     * the Worlds and place them into the HashMap & Custom Class.
     */
    public void loadWorlds() {
        /**
         * Simple counter so we can output how many Worlds were loaded.
         */
        int count = 0;
        /**
         * Grab all the Worlds from the 'worlds.yml' we already loaded this file
         * into 'configWorlds' during the onEnable stage.
         */
        List<String> worldKeys = this.configWorlds.getKeys("worlds");
        /**
         * If we have no Worlds listed in the file then we can skip the
         * following.
         */
        if (worldKeys != null) {
            /**
             * For each entry within the List of Worlds we perform the
             * following.
             */
            for (String worldKey : worldKeys) {
                /**
                 * Grab the Environment type - Normal/Nether - Default = Normal
                 */
                String wEnvironment = this.configWorlds.getString("worlds." + worldKey + ".environment", "NORMAL");
                /**
                 * Grab the Mob Setting - True/False - Default = true
                 */
                Boolean mobs = this.configWorlds.getBoolean("worlds." + worldKey + ".mobs", true);
                /**
                 * Grab the Animals Setting - True/False - Default = true
                 */
                Boolean animals = this.configWorlds.getBoolean("worlds." + worldKey + ".animals", true);
                /**
                 * We need to take the Environment type and grab the real
                 * environment ENUM.
                 */
                Environment env;
                if (wEnvironment.equalsIgnoreCase("NETHER")) {
                    env = Environment.NETHER;
                } else {
                    env = Environment.NORMAL;
                }
                /**
                 * Output that we are loading a world with a specific
                 * environment type.
                 */
                log.info(logPrefix + "Loading World & Settings - '" + worldKey + "' - " + wEnvironment);
                /**
                 * Tell the server to create the world then place it within a
                 * variable so we can pass this onto our HashMap.
                 */
                World world = this.getServer().createWorld(worldKey, env);
                /**
                 * Quick work around to set the Mob/Animal settings.
                 */
                ((CraftWorld) world).getHandle().allowMonsters = mobs;
                ((CraftWorld) world).getHandle().allowAnimals = animals;
                /**
                 * Place the World into hour HashMap with our Custom Class, the
                 * custom class also gets passed along the config file so it can
                 * edit it and save it, as well as this class.
                 */
                MVWorlds.put(worldKey, new MVWorld(world, this.configWorlds, this));
                /**
                 * Increment the World Count.
                 */
                count++;
            }
        }
        /**
         * If the config file does not contain our default world then we will
         * load it anyways this is needed because all commands based on Worlds
         * are checked via the HashMap.
         */
        if (!this.MVWorlds.containsKey(getServer().getWorlds().get(0).getName())) {
            /**
             * Grab the World at ID 0, this will always be the default world.
             */
            World world = getServer().getWorlds().get(0);
            /**
             * Another simple Output.
             */
            log.info(logPrefix + "Loading World & Settings - '" + world.getName() + "' - " + world.getEnvironment());
            /**
             * Grab the Worlds Spawn location.
             */
            String location = this.utils.locationToString(world.getSpawnLocation());
            /**
             * Place the World into our HashMap and then force a save of the
             * config file, this makes it so the default world and it's settings
             * are then saved into the config.
             */
            this.MVWorlds.put(world.getName(), new MVWorld(world, this.configWorlds, this));
            this.MVWorlds.get(world.getName()).setMobSpawn(((CraftWorld) world).getHandle().allowMonsters);
            this.MVWorlds.get(world.getName()).setAnimalSpawn(((CraftWorld) world).getHandle().allowAnimals);
            this.MVWorlds.get(world.getName()).saveAll();
            /**
             * Increment the count again.
             */
            count++;
        }
        /**
         * Output the amount of Worlds loaded onto the Server.
         */
        log.info(logPrefix + count + " - World(s) loaded.");
    }

    /**
     * This section is run once when the Plugin is enabled, this will load all
     * the Portals and place them into the HashMap & Custom Class.
     */
    private void loadPortals() {
        /**
         * Simple Output stating we are now Loading the Portals.
         */
        log.info(logPrefix + "Loading Portals");
        /**
         * Create a list populated with all the Portals in our config file.
         */
        List<String> portalKeys = this.configPortals.getKeys("portals");
        /**
         * If the list is empty then we don't have to do anything.
         */
        if (portalKeys == null) {
            log.info(logPrefix + 0 + " - Portal(s) loaded.");
            return;
        }
        /**
         * Initialise a variable to hold the portal count.
         */
        int count = 0;
        /**
         * Loop through all the entries within the list and load the settings
         * into our HashMap with its Custom Class.
         */
        for (String portalKey : portalKeys) {
            /**
             * Key = Portal name and we pass along the config file and this
             * class.
             */
            String world = this.configPortals.getString("portals." + portalKey + ".world");
            if (getServer().getWorld(world) != null) {
                MVPortals.put(portalKey, new MVPortal(portalKey, this.configPortals, this));
                /**
                 * Increment the Portal Count.
                 */
                count++;
            }
        }
        /**
         * Output the amount of Portals loaded to the log.
         */
        log.info(logPrefix + count + " - Portal(s) loaded.");
    }

    public boolean checkPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (test != null) {
            Permissions = com.nijikokun.bukkit.Permissions.Permissions.Security;
            return true;
        } else {
            return false;
        }
    }

    /**
     * This section will check whether iConomy is enabled.
     * 
     * @return
     */
    public boolean checkiConomy() {
        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        if (test != null) {
            log.info("[MultiVerse] Found iConomy, enabling payments.");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Simple Script which runs when the Plugin is disabled... Currently we just
     * use this to output to the user the fact it's disabled and to stop the
     * updateChecker if one was initialised. We can't unregister events atm.
     */
    @Override
    public void onDisable() {
        /**
         * If updateCheck is NOT NULL then it means we have enabled Update
         * Checking, this needs to be cancelled otherwise even though the plugin
         * is disabled they will still get alerts telling them it's out of date.
         */
        if (this.updateCheck != null) {
            this.updateCheck.timer.cancel();
        }
        log.info(logPrefix + "- Disabled");
    }

}
