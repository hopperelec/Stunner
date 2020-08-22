package uk.co.hopperlec.mc.stun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {
    private final List<Stun> stuns = new ArrayList<>();
    public List<Stun> getPlayers() {
        return stuns;
    }
    public String defaultReason;
    public int defaultSeconds;
    public boolean defaultRotate;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        defaultReason = getConfig().getString("defaultReason");
        if (defaultReason == null) {
            System.out.println("Config error: defaultReason. Cancelling plugin start-up!");
            this.setEnabled(false); return;}
        try {defaultSeconds = getConfig().getInt("defaultSeconds");
        } catch(NumberFormatException e) {
            System.out.println("Config error: defaultSeconds. Cancelling plugin start-up!");
            this.setEnabled(false);}
        defaultRotate = getConfig().getBoolean("defaultRotate");

        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public boolean onCommand(CommandSender author, Command cmd, String label, String[] args) {
        if (author.hasPermission("hopperstunner.stun")) {
            if (label.equalsIgnoreCase("stun")) {
                if (args.length >= 1) {
                    final Player player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        if (getStunFromPlayer(player) == null) {
                            int seconds = defaultSeconds; StringBuilder reason = new StringBuilder(defaultReason); boolean rotate = defaultRotate;
                            if (args.length >= 2) {
                                try {seconds = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {author.sendMessage("Invalid integer '"+args[1]+"'!"); return false;}
                                if (args.length >= 3) {
                                    rotate = Boolean.parseBoolean(args[2]);
                                    if (args.length >= 4) {
                                        reason = new StringBuilder();
                                        for (int x=3; x <= args.length-1; x++) reason.append(args[x]);}}}
                            Stun stun = new Stun(player,seconds, reason.toString(),rotate);
                            stuns.add(stun);
                            author.sendMessage(player.getDisplayName()+" has been stunned for "+seconds+" seconds for '"+reason+"' with rotate "+rotate);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                                stuns.remove(stun);
                                author.sendMessage(player.getDisplayName()+" has been unstunned");
                            }, seconds*20L);
                        } else author.sendMessage("Player '"+player.getDisplayName()+"' is already stunned!");
                    } else author.sendMessage("Could not find a player by the name '"+args[0]+"'!");
                } else author.sendMessage("Format: /stun (player) [seconds] [rotate] [reason]");
            } else {
                if (args.length >= 1) {
                    final Player player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        Stun stun = getStunFromPlayer(player);
                        if (stun != null) {
                            stuns.remove(stun);
                            author.sendMessage(player.getDisplayName()+" has been unstunned");}
                        else author.sendMessage("Player '"+player.getDisplayName()+"' isn't already stunned!");
                    } else author.sendMessage("Could not find a player by the name '"+args[0]+"'!");
                } else author.sendMessage("Format: /unstun");
            }
        } else author.sendMessage("Insufficient permission!");
        return false;
    }

    public Stun getStunFromPlayer(Player player) {
        for (Stun stun : stuns) if (stun.player == player) return stun;
        return null;
    }

    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {
        Stun stun = getStunFromPlayer(event.getPlayer());
        if (stun != null) {
            if (stun.rotate) {
                Location oldTo = event.getTo();
                if (oldTo == null) {
                    System.out.println("Could not correctly prevent movement of player '"+event.getPlayer().getDisplayName()+"'!");
                    event.setCancelled(true); return;}
                Location newTo = event.getFrom();
                newTo.setPitch(oldTo.getPitch());
                newTo.setYaw(oldTo.getYaw());
                event.setTo(newTo);
            } else event.setCancelled(true);}
    }
}
