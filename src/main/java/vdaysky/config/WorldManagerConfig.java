package vdaysky.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import vdaysky.util.Time;

import java.io.File;
import java.io.IOException;

public class WorldManagerConfig {

    private static WorldManagerConfig instance;

    private static final String CONFIG_FILE = "config.yml";
    private static final String WORLD_UNLOAD_CHECK_DELAY_FIELD = "world-unload-check-delay-s";
    private static final String WORLD_UNLOAD_DELAY_FIELD = "world-unload-delay-s";
    private static final long DEFAULT_WORLD_UNLOAD_CHECK_DELAY = Time.fromMinutes(30).toSeconds();
    private static final long DEFAULT_WORLD_UNLOAD_DELAY = Time.fromDays(7).toSeconds();

    private final YamlConfiguration config;

    private WorldManagerConfig(YamlConfiguration config) {
        this.config = config;
    }

    public Time getWorldUnloadCheckDelay() {
        var delayS = config.getLong(WORLD_UNLOAD_CHECK_DELAY_FIELD, DEFAULT_WORLD_UNLOAD_CHECK_DELAY);
        return Time.fromSeconds(delayS);
    }

    public Time getWorldUnloadDelay() {
        var delayS = config.getLong(WORLD_UNLOAD_DELAY_FIELD, DEFAULT_WORLD_UNLOAD_DELAY);
        return Time.fromSeconds(delayS);
    }

    private static YamlConfiguration createDefault() {
        YamlConfiguration config = new YamlConfiguration();
        config.set(WORLD_UNLOAD_CHECK_DELAY_FIELD, DEFAULT_WORLD_UNLOAD_CHECK_DELAY);
        config.set(WORLD_UNLOAD_DELAY_FIELD, DEFAULT_WORLD_UNLOAD_DELAY);
        return config;
    }

    public static WorldManagerConfig getInstance() {
        if (instance == null) {
            File f = new File(Bukkit.getPluginManager().getPlugin("WorldManager").getDataFolder(), CONFIG_FILE);

            if (!f.exists()) {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    createDefault().save(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            YamlConfiguration config = new YamlConfiguration();

            try {
                config.load(f);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            instance = new WorldManagerConfig(config);
        }
        return instance;
    }

    public static void unload() {
        instance = null;
    }

}
