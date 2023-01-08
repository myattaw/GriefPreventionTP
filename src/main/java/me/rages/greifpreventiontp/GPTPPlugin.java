package me.rages.greifpreventiontp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.lucko.helper.Commands;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author : Michael
 * @since : 10/10/2022, Monday
 **/
@Plugin(
        name = "GriefPreventionTP",
        softDepends = {"floodgate"},
        hardDepends = {"helper", "GriefPrevention"},
        apiVersion = "1.19"
)
public class GPTPPlugin extends ExtendedJavaPlugin {

    private SignMenuFactory signMenuFactory;

    private Map<Long, String> claimRenameMap = new HashMap<>();

    public boolean useFloodgateUI = false;

    @Override
    protected void enable() {

        saveDefaultConfig();
        loadRenameData();

        if (!getServer().getPluginManager().isPluginEnabled("GriefPrevention")) {
            getLogger().log(Level.SEVERE, "Could not find GriefPrevention plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (getServer().getPluginManager().isPluginEnabled("floodgate")) {
            useFloodgateUI = true;
        }

        this.signMenuFactory = new SignMenuFactory(this);

        Commands.create().assertPermission("griefpreventiontp.use")
                .assertPlayer()
                .handler(cmd -> new ClaimsUI(this, cmd.sender(), "Teleport Menu").open())
                .registerAndBind(this, new String[]{"claims", "griefpreventiontp", "gptp"});

        Schedulers.async().runRepeating(() -> saveRenameData(), 12000L, 12000L);
    }

    @Override
    protected void disable() {
        saveRenameData();
    }

    private void loadRenameData() {
        Map<Long, String> configData = null;
        if (Files.isReadable(Paths.get(getDataFolder() + File.separator + "rename_data.json"))) {
            try (Reader reader = new FileReader(getDataFolder() + File.separator + "rename_data.json")) {
                configData = new Gson().fromJson(reader, new TypeToken<Map<Long, String>>() {
                }.getType());
            } catch (IOException e) {
                getServer().getLogger().log(Level.SEVERE, "Failed to load rename_data.json!");
            }
            if (configData != null) {
                this.claimRenameMap = configData;
            }
        }
    }

    private void saveRenameData() {
        try (FileWriter writer = new FileWriter(getDataFolder() + File.separator + "rename_data.json")) {
            new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(getClaimRenameMap(), writer);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save rename data!");
        }
    }

    public void promptPlayer(Claim claim, Player target) {
        SignMenuFactory.Menu menu = signMenuFactory.newMenu(Arrays.asList("", "^^^^^^^^^^^^^^^", "Please Type Name", "---------------"))
                .response((player, strings) -> {
                    if (strings[0].length() == 0) {
                        player.sendMessage(ChatColor.RED + "Failed to type a proper claim name!");
                        return false;
                    }
                    getClaimRenameMap().put(claim.getID(), strings[0]);
                    return true;
                });
        menu.open(target);
    }

    public Map<Long, String> getClaimRenameMap() {
        return claimRenameMap;
    }
}
