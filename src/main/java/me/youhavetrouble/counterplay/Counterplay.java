package me.youhavetrouble.counterplay;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.youhavetrouble.counterplay.listeners.PlayerTakingDamageListener;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.damage.DamageType;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class Counterplay extends JavaPlugin {

    private final FileConfiguration fileConfiguration = new YamlConfiguration();

    private boolean oneshotProtectionEnabled;
    private double oneshotProtectionHealthThreshold = 10.0;
    private final Set<DamageType> oneshotProtectionExcludedDamageTypes = new HashSet<>();

    @Override
    public void onEnable() {
        try {
            reloadPluginConfig();
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerTakingDamageListener(this), this);
        LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("counterplay")
                            .then(Commands.literal("reload")
                                    .requires(sender -> {
                                        if (sender instanceof Permissible permissible) {
                                            return permissible.hasPermission("counterplay.reload");
                                        }
                                        return true;
                                    })
                                    .executes(context -> {
                                        try {
                                            reloadPluginConfig();
                                            context.getSource().getSender().sendMessage("Reloaded counterplay config.");
                                        } catch (IOException e) {
                                            context.getSource().getSender().sendRichMessage("<red>Failed to reload config: " + e.getMessage());
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .build()
            );

        });
    }

    public void reloadPluginConfig() throws IOException {
        File file = new File(getServer().getPluginsFolder() + "/Counterplay/config.yml");

        try {
            file.getParentFile().mkdirs();
            fileConfiguration.load(file);
        } catch (IOException e) {
            getLogger().warning("Config not found, creating a new one.");
            fileConfiguration.save(file);
        } catch (InvalidConfigurationException e) {
            getLogger().severe("config.yml is invalid: " + e.getMessage());
        }

        try {

            this.oneshotProtectionEnabled = getBoolean("oneshot-protection.enabled", false);
            this.oneshotProtectionHealthThreshold = getDouble("oneshot-protection.health-threshold", 10);
            List<String> damageTypes = getStringList("oneshot-protection.excluded-damage-types", List.of(
                    DamageType.GENERIC_KILL.key().asMinimalString(),
                    DamageType.FALL.key().asMinimalString(),
                    DamageType.FLY_INTO_WALL.key().asMinimalString()
            ));
            oneshotProtectionExcludedDamageTypes.clear();
            for (String damageType : damageTypes) {
                try {
                    Key key = Key.key(damageType);
                    DamageType type = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).get(key);
                    oneshotProtectionExcludedDamageTypes.add(type);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid damage type: " + damageType);
                }
            }
            fileConfiguration.save(file);
        } catch (IOException e) {
            getLogger().warning("Failed to save config.yml: " + e.getMessage());
        }
    }

    private boolean getBoolean(String path, boolean def) {
        if (fileConfiguration.isBoolean(path)) return fileConfiguration.getBoolean(path);
        fileConfiguration.set(path, def);
        return def;
    }

    private double getDouble(String path, double def) {
        if (fileConfiguration.isDouble(path)) return fileConfiguration.getDouble(path);
        fileConfiguration.set(path, def);
        return def;
    }

    private List<String> getStringList(String path, List<String> def) {
        if (fileConfiguration.isList(path)) return fileConfiguration.getStringList(path);
        fileConfiguration.set(path, def);
        return def;
    }

    public boolean isOneshotProtectionEnabled() {
        return oneshotProtectionEnabled;
    }

    public double getOneshotProtectionHealthThreshold() {
        return oneshotProtectionHealthThreshold;
    }

    public Set<DamageType> getOneshotProtectionExcludedDamageTypes() {
        return oneshotProtectionExcludedDamageTypes;
    }

}
