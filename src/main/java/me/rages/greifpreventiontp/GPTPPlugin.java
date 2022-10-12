package me.rages.greifpreventiontp;

import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;

import java.util.logging.Level;

/**
 * @author : Michael
 * @since : 10/10/2022, Monday
 **/
@Plugin(
        name = "GriefPreventionTP",
        hardDepends = {"helper", "GriefPrevention"},
        apiVersion = "1.19"
)
public class GPTPPlugin extends ExtendedJavaPlugin {

    @Override
    protected void enable() {

        saveDefaultConfig();

        if (!getServer().getPluginManager().isPluginEnabled("GriefPrevention")) {
            getLogger().log(Level.SEVERE, "Could not find GriefPrevention plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }

        Commands.create().assertPermission("griefpreventiontp.use")
                .assertPlayer()
                .handler(cmd -> {
                    new GPMenu(this, cmd.sender(), 5, "Teleport Menu").open();
                }).registerAndBind(this, new String[]{"griefpreventiontp", "gptp"});
    }



}
