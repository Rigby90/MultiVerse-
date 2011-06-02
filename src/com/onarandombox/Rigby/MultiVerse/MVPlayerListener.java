package com.onarandombox.Rigby.MultiVerse;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;

import com.iConomy.*;
import com.iConomy.system.Holdings;

@SuppressWarnings("unused")
public class MVPlayerListener extends PlayerListener {
    private BukkitScheduler scheduler;
    /**
     * Initialise variables to hold the Server, Plugin and Configuration.
     */
    private final MultiVerse plugin;
    private final Server server;
    private Configuration configuration;
    /**
     * Initialise our variables to hold MV Custom classes, our teleport class
     * which handles all player teleports and our Utilities class which has some
     * useful functions.
     */
    private MVTeleport playerTeleporter;
    private MVUtils utils;
    /**
     * Initialise our Logger to log outputs to the Minecraft Console and Log
     * File.
     */
    private final Logger log = Logger.getLogger("Minecraft");

    /**
     * Construct our PlayerListener.
     * 
     * @param instance
     * @param configMV
     */
    public MVPlayerListener(MultiVerse instance, Configuration configMV) {
        /**
         * Setup our Plugin, Server and Configuration variables which we
         * initialised.
         */
        this.plugin = instance;
        this.server = instance.getServer();
        this.configuration = configMV;
        /**
         * Create new instances of MVUtils and MVTeleport and assign them to our
         * variables.
         */
        this.utils = new MVUtils(instance);
        this.playerTeleporter = new MVTeleport(this.plugin);
    }

    /**
     * On player join we need to setup a playerSession so we can assign
     * Coordinates etc to.
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        this.plugin.playerSessions.put(event.getPlayer().getName(), new MVPlayerSession(event.getPlayer(), this.configuration));
    }

    /**
     * On player quit we remove their sessions to stop it from clogging up the
     * server.
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        this.plugin.playerSessions.remove(p.getName());
    }

    /**
     * On player respawn, we'll act upon the settings whether to respawn them in
     * the world or not.
     */
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        /**
         * Grab the Player and World.
         */
        Player player = event.getPlayer();
        World world = player.getWorld();
        /**
         * Start off with a blank Location, this allows us to error check.
         */
        Location l = null;
        /**
         * Check whether the server has GlobalRespawn enabled, by default this
         * is false. If true we grab the Respawn point regardless of what world
         * they are in.
         */
        if (this.configuration.getBoolean("globalrespawn", false)) {
            l = playerTeleporter.getDestination(this.plugin.MVWorlds.get(world.getName()).getSpawnLocation(), player);
        }
        /**
         * If GlobalRespawn is disabled then we check if they have Alternate
         * Respawning setup, if they are we get the location and assign it to
         * 'l'. Otherwise we do nothing.
         */
        else {
            if (this.configuration.getBoolean("alternaterespawn", false)) {
                if (!world.getName().equals(this.plugin.getServer().getWorlds().get(0).getName())) {
                    l = playerTeleporter.getDestination(this.plugin.MVWorlds.get(world.getName()).getSpawnLocation(), player);
                }
            }
        }
        /**
         * If the user is within the SPLike world and the RespawnToDefault
         * setting is turned on. We will respawn them back to the default World.
         */
        /*
         * if (event.getPlayer().getWorld().getName()
         * .equalsIgnoreCase(this.configuration.getString("nether"))) { if
         * (this.configuration.getBoolean("respawntodefault", true) &&
         * this.configuration.getBoolean("splike", false)) { l =
         * this.plugin.MVWorlds.get(
         * this.plugin.getServer().getWorlds().get(0).getName())
         * .getSpawnLocation(); } else { l = null; } }
         */
        /**
         * If both GlobalRespawn and AlternateRespawn are disabled then 'l' will
         * still be NULL so we do nothing, otherwise we teleport the player to
         * the location.
         */
        if (l != null) {
            event.setRespawnLocation(l);
            player.teleport(l);
        }
    }

    /**
     * On player chat, if the server is set to prefix the chat or not.
     */
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        /**
         * Check whether the Server is set to prefix the chat with the World
         * name. If not we do nothing, if so we need to check if the World has
         * an Alias.
         */
        if (this.configuration.getBoolean("prefix", false)) {
            /**
             * Grab the name of the World the player is in.
             */
            String world = event.getPlayer().getWorld().getName();
            /**
             * Setup a String called 'prefix'
             */
            String prefix;
            /**
             * Check whether the Alias for the world is not empty and is longer
             * than 0 Letters. If so we apply it to the 'prefix' String.
             */
            // TODO: Check World exists first.
            if (this.plugin.MVWorlds.get(world).getAlias() != "" && this.plugin.MVWorlds.get(world).getAlias().length() > 0) {
                prefix = this.plugin.MVWorlds.get(world).getAlias();
            }
            /**
             * If the World doesn't have a prefix we'll just sent the full World
             * name, can get messy :).
             */
            else {
                prefix = world;
            }
            /**
             * Format the output of the String to add in the Prefix before the
             * rest of the message.
             */
            String format = event.getFormat();
            /**
             * Set the formatting.
             */
            event.setFormat("[" + prefix + "]" + format);
        }
    }

    /**
     * On player move, detect they are inside a portal then teleport them
     * appropriately.
     */
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        /**
         * Grab the Player and our Players Session
         */
        final Player pl = event.getPlayer();
        MVPlayerSession ps = this.plugin.playerSessions.get(pl.getName());
        if(ps==null){
            this.plugin.reloadPlayerSessions();
            ps = this.plugin.playerSessions.get(pl.getName());
        }
        Location poloc = ps.loc;
        Location pnloc = pl.getLocation();
        if (ps.loc != null) {
            if (poloc.getBlockX() == pnloc.getBlockX() && poloc.getBlockY() == pnloc.getBlockY() && poloc.getBlockZ() == pnloc.getBlockZ()) {
                return;
            } else {
                ps.loc = pl.getLocation();
            }
        } else {
            ps.loc = pl.getLocation();
        }
        /**
         * Start the Price off at 0, this will change according to the
         * Portal/World Settings.
         */
        Integer price = 0;
        /**
         * Start of our Location as NULL, this allows us to check it later on.
         */
        Location d = null;
        /**
         * First we do a check against all the Portals we have created, if the
         * area the user is within is a Portal then we will act upon it; if not
         * then we move onto our next check.
         */
        String ptest = utils.isPortal(pl.getLocation());
        if (ptest != null) {
            MVPortal p = this.plugin.MVPortals.get(ptest);
            price = (int) Math.round(p.getPrice());
            d = playerTeleporter.portalDestination(pl, ptest, p);
        }
        /**
         * End of First Portal Check.
         */

        /**
         * If the first Portal Check failed then we will check for Any Signs
         * around the player. This check is only performed if the user is
         * standing inside a Portal Block.
         */
        if (this.plugin.configMV.getBoolean("checksigns", true) && d == null) {
            d = playerTeleporter.portalSignMethod(pl);
        }
        /**
         * End of Sign Based Portal Check.
         */

        /**
         * Standard Nether Portal Check, this will be for a Single Player like
         * feel, customizeable... Can be on or off.
         */
        if (this.plugin.configMV.getBoolean("splike", false) && d == null) {
            d = playerTeleporter.portalSPNether(pl);
        }
        /**
         * End of Single Player Nether Check.
         */

        // TODO: Permissions to add here...
        /**
         * If we have a Location set and it is NOT NULL then we can perform a
         * teleport.
         */
        if (d != null) {
            if (!ps.getTeleportable()) {
                return;
            }
            if (!playerTeleporter.canTravelFromWorld(pl, d.getWorld())) {
                ps.sendMessage("Sorry but you cannot travel to '" + d.getWorld().getName() + "' from this World!");
                return;
            }
            if (!playerTeleporter.canEnterWorld(pl, d.getWorld())) {
                ps.sendMessage("Sorry but you cannot enter the '" + d.getWorld().getName() + "' world.");
                return;
            }
            if (MultiVerse.useiConomy && !MultiVerse.Permissions.has(pl, "multiverse.portal.exempt") && price > 0) {
                Holdings balance = iConomy.getAccount(pl.getName()).getHoldings();
                if (balance.hasEnough(price)) {
                    balance.subtract(price);
                    pl.sendMessage(ChatColor.RED + this.plugin.logPrefix + " You have been charged " + iConomy.format(price));
                } else {
                    if (ps.getAlertable()) {
                        pl.sendMessage("Sorry but you do not have the required funds for this portal");
                        ps.setAlertCooldown();
                    }
                    return;
                }
            }
            final Location destination = d;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    pl.teleport(destination);
                }
            });
            ps.setTPCooldown();
            return;
        }
        return;
    }

    /**
     * Event - onBlockRightClick - If a player right clicks a block check their
     * permissions and set a Coordinate.
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getItemInHand().getTypeId() == this.plugin.configMV.getInt("setwand", 270) && MultiVerse.Permissions.has(player, "multiverse.portal.create")) {
            String[] whitelist = this.plugin.configMV.getString("portalblocks", "").split(",");
            if (whitelist.length > 0 && whitelist[0] != "") {
                for (String element : whitelist) {
                    int w = Integer.valueOf(element);
                    if (w != event.getClickedBlock().getTypeId()) {
                        return;
                    }
                }
            }
            Location l = event.getClickedBlock().getLocation();
            if (this.plugin.playerSessions.get(player.getName()).compareLocation2(l)) {
                return;
            }
            this.plugin.playerSessions.get(player.getName()).setLocation2(l);
            player.sendMessage("Position 2 - Set");
        }
    }

    /*
     * public void timedTeleport(final Player pl, final Location d, final int
     * delay, final Integer price){ Timer timer = new Timer();
     * if(this.plugin.playerSessions.get(pl.getName()).timer==false){
     * 
     * this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin
     * , new Runnable() {
     * 
     * public void run() { if(delay>0){ if(MultiVerse.useiConomy){
     * pl.sendMessage(ChatColor.RED + "Portal Cost - " + price + " " +
     * iConomy.currency); } pl.sendMessage(ChatColor.RED + "You have " + delay +
     * " Second(s) to leave the teleporter."); for(int i = delay; i>0; i--){
     * pl.sendMessage(ChatColor.AQUA + "" + i + " Second(s) remaining!"); try {
     * Thread.sleep(1000); } catch (InterruptedException e) { } } }
     * finalCheck(pl,d,price); }
     * 
     * }, 0L); } this.plugin.playerSessions.get(pl.getName()).timer = true; }
     * 
     * public void finalCheck(final Player pl, final Location d, Integer price){
     * Location l = pl.getLocation(); String ptest =
     * utils.isPortal(pl.getLocation()); if((ptest!=null)){ MVPlayerSession ps =
     * plugin.playerSessions.get(pl.getName()); ps.timer = false;
     * ps.setTPCooldown(); ps.teleporting = true;
     * MultiVerse.server.getPluginManager().callEvent(new
     * PlayerMoveEvent(Event.Type.PLAYER_MOVE, pl, d, d));
     * pl.sendMessage(ChatColor.GREEN + "Teleportation Successful");
     * 
     * if(MultiVerse.useiConomy){ if
     * (iConomy.db.get_balance(pl.getName())>price){ int balance =
     * iConomy.db.get_balance(pl.getName()); int amount = price;
     * iConomy.db.set_balance(pl.getName(), balance-amount);
     * pl.sendMessage(ChatColor.RED + this.plugin.logPrefix +
     * " You have been charged " + amount + " " + iConomy.currency); } else {
     * if(ps.getAlertable()){
     * pl.sendMessage("Sorry but you do not have the required funds for this portal"
     * ); ps.setAlertCooldown(); } return; } }
     * 
     * } else { pl.sendMessage(ChatColor.RED + "Teleportation Cancelled"); }
     * this.plugin.playerSessions.get(pl.getName()).timer = false; }
     */
}