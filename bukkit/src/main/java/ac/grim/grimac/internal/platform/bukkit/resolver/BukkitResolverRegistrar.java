package ac.grim.grimac.internal.platform.bukkit.resolver;

import ac.grim.grimac.api.plugin.BasicGrimPlugin;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.internal.plugin.resolver.GrimExtensionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BukkitResolverRegistrar {
    private final Map<Plugin, GrimPlugin> pluginCache = new ConcurrentHashMap<Plugin, GrimPlugin>();

    public void registerAll(GrimExtensionManager manager) {
        manager.setFailureHandler(this::createFailureException);
        manager.registerResolver(this::resolvePluginInstance);
        manager.registerResolver(this::resolveStringName);
        manager.registerResolver(this::resolveClass);
    }

    public GrimPlugin resolvePlugin(Plugin plugin) {
        GrimPlugin cached = pluginCache.get(plugin);
        if (cached != null) {
            return cached;
        }
        PluginDescriptionFile description = plugin.getDescription();
        GrimPlugin created = new BasicGrimPlugin(
                plugin.getLogger(),
                plugin.getDataFolder(),
                description.getVersion(),
                description.getDescription(),
                description.getAuthors()
        );
        GrimPlugin previous = pluginCache.putIfAbsent(plugin, created);
        return previous != null ? previous : created;
    }

    private GrimPlugin resolvePluginInstance(Object object) {
        if (object instanceof Plugin) {
            return resolvePlugin((Plugin) object);
        }
        return null;
    }

    private GrimPlugin resolveStringName(Object object) {
        if (object instanceof String) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin((String) object);
            return plugin == null ? null : resolvePlugin(plugin);
        }
        return null;
    }

    private GrimPlugin resolveClass(Object object) {
        if (object instanceof Class) {
            try {
                return resolvePlugin(JavaPlugin.getProvidingPlugin((Class<?>) object));
            } catch (IllegalArgumentException ignored) {
                return null;
            } catch (IllegalStateException ignored) {
                return null;
            }
        }
        return null;
    }

    private RuntimeException createFailureException(Object object) {
        String type = object == null ? "null" : object.getClass().getName();
        return new IllegalArgumentException(String.format(
                "Failed to resolve GrimPlugin context from the provided object of type '%s'.\n\n" +
                        "Please ensure you are passing one of the following:\n" +
                        "  - The main instance of your plugin (e.g., 'this' from your class extending JavaPlugin).\n" +
                        "  - The plugin name as a String (e.g., \"MyPluginName\").\n" +
                        "  - Any Class from your plugin's JAR file (e.g., MyListener.class).\n" +
                        "  - A pre-existing GrimPlugin instance.\n",
                type
        ));
    }
}
