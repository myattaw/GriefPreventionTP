package me.rages.greifpreventiontp;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.Iterator;
import java.util.List;

/**
 * @author : Michael
 * @since : 10/11/2022, Tuesday
 **/
public class GPMenu extends Gui {

    private static final MenuScheme SCHEME = new MenuScheme()
            .maskEmpty(1)
            .mask("111111111")
            .mask("111111111")
            .mask("111111111")
            .maskEmpty(1);

    private GPTPPlugin plugin;

    private List<String> claimLore;

    public GPMenu(GPTPPlugin plugin, Player player, int lines, String title) {
        super(player, lines, title);
        this.plugin = plugin;
        this.claimLore = plugin.getConfig().getStringList("guis.claim.lore");
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {
            fillWith(ItemStackBuilder.of(new MaterialData(Material.GRAY_STAINED_GLASS_PANE, (byte) 0).toItemStack(1))
                    .name(plugin.getConfig().getString("guis.main.background.display-name", " "))
                    .build(null)
            );
        }


        Iterator<Integer> slotIter = SCHEME.getMaskedIndexes().iterator();

        int count = 1;

        for (Claim claim : GriefPrevention.instance.dataStore.getPlayerData(getPlayer().getUniqueId()).getClaims()) {

            ItemStackBuilder builder = ItemStackBuilder.of(Material.PLAYER_HEAD)
                    .data(3)
                    .name(ChatColor.GREEN + "Unnamed Claim (" + count++ + ")")
                    .transformMeta(meta -> ((SkullMeta) meta).setOwner(getPlayer().getName()));

            Location min = claim.getLesserBoundaryCorner();
            Location max = claim.getGreaterBoundaryCorner();
            Location location = new Location(
                    min.getWorld(),
                    (min.getBlockX() + max.getBlockX()) / 2,
                    (min.getBlockY() + max.getBlockY()) / 2,
                    (min.getBlockZ() + max.getBlockZ()) / 2
            );

            for (String lore : claimLore) {
                builder.lore(lore
                        .replace("%owner%", claim.getOwnerName())
                        .replace("%world%", claim.getGreaterBoundaryCorner().getWorld().getName())
                        .replace("%size%", String.format("%,d", claim.getArea()))
                        .replace("%x%", String.format("%.1f", location.getX()))
                        .replace("%y%", String.format("%.1f", location.getY()))
                        .replace("%z%", String.format("%.1f", location.getZ()))
                );
            }

            setItem(slotIter.next(), builder.build(() -> {
                // right click
                getPlayer().teleport(location.toHighestLocation().add(0.5, 1, 0.5));
            }, () -> {
                // left click
                getPlayer().teleport(location.toHighestLocation().add(0.5, 1, 0.5));
            }));
        }


    }


}
