package me.youhavetrouble.counterplay.listeners;

import me.youhavetrouble.counterplay.Counterplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerTakingDamageListener implements Listener {

    private final Counterplay plugin;

    public PlayerTakingDamageListener(Counterplay plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTakingDamage(EntityDamageEvent event) {
        if (!plugin.isOneshotProtectionEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getOneshotProtectionExcludedDamageTypes().contains(event.getDamageSource().getDamageType())) return;
        if (player.getHealth() < plugin.getOneshotProtectionHealthThreshold()) return;
        if (event.getFinalDamage() < player.getHealth()) return;
        double damage = event.getFinalDamage();
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (!event.isApplicable(modifier)) continue;
            event.setDamage(modifier, 0);
        }
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, Math.max(0, Math.min(damage, player.getHealth() - 1)));
    }

}
