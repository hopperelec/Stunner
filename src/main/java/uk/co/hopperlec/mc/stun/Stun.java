package uk.co.hopperlec.mc.stun;

import org.bukkit.entity.Player;

public class Stun {
    public Player player;
    private final int seconds;
    public String reason;
    public boolean rotate;

    public int getSeconds() {
        return seconds;
    }

    public Stun(Player player, int seconds, String reason, boolean rotate) {
        this.player = player; this.seconds = seconds; this.reason = reason; this.rotate = rotate;
    }
}
