package me.rages.greifpreventiontp.bedrock;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.FunctionalSchemeMapping;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.rages.greifpreventiontp.GPTPPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

/**
 * @author : Michael
 * @since : 10/12/2022, Wednesday
 **/
public class BedrockUI extends Gui {

    private static final int[] TELEPORT_SLOTS = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21};
    private static final int[] RENAME_SLOTS = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26};

    private GPTPPlugin plugin;
    private Claim claim;
    private Location location;

    public BedrockUI(GPTPPlugin plugin, Claim claim, Location location, Player player) {
        super(player, 3, "&a&lTELEPORT &7&l& &6&lRENAME");
        this.plugin = plugin;
        this.claim = claim;
        this.location = location;
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {

            fillWith(ItemStackBuilder.of(new MaterialData(Material.GRAY_STAINED_GLASS_PANE, (byte) 0).toItemStack(1)).name("").build(null));

            Item teleportItem = ItemStackBuilder.of(Material.ENDER_EYE)
                    .name("&a&lCLICK TO TELEPORT")
                    .build(() -> getPlayer().teleport(location.toHighestLocation().add(0.5, 1, 0.5)));

            Item renameItem = ItemStackBuilder.of(Material.WRITABLE_BOOK)
                    .name("&6&lCLICK TO RENAME")
                    .build(() -> plugin.promptPlayer(claim, getPlayer()));

            Arrays.stream(TELEPORT_SLOTS).forEach(i -> setItem(i, teleportItem));
            Arrays.stream(RENAME_SLOTS).forEach(i -> setItem(i, renameItem));
        }
    }
}
