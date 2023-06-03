package me.rages.greifpreventiontp;

import com.google.common.collect.ImmutableMap;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author : Michael
 * @since : 10/11/2022, Tuesday
 **/
public class ClaimsUI extends Gui {

    private static final MenuScheme SCHEME = new MenuScheme()
            .mask("111111111")
            .mask("111111111")
            .mask("111111111")
            .mask("111111111")
            .mask("111111111")
            .mask("111111111");

    private GPTPPlugin plugin;

    private List<String> claimLoreJava;
    private List<String> claimLoreBedrock;
    private int page;

    private Map<World.Environment, Material> CLAIM_ITEM_TYPE = ImmutableMap.of(
            World.Environment.NORMAL, Material.GRASS_BLOCK,
            World.Environment.NETHER, Material.NETHERRACK,
            World.Environment.THE_END, Material.END_STONE,
            World.Environment.CUSTOM, Material.PURPLE_CONCRETE
    );

    public ClaimsUI(GPTPPlugin plugin, Player player, String title, int page) {
        super(player, 6, title);
        this.plugin = plugin;
        this.claimLoreJava = plugin.getConfig().getStringList("guis.claim-java.lore");
        this.claimLoreBedrock = plugin.getConfig().getStringList("guis.claim-bedrock.lore");
        this.page = page;
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

        Vector<Claim> claims = GriefPrevention.instance.dataStore.getPlayerData(getPlayer().getUniqueId()).getClaims();

        int i = page * 45;
        while (i < ((page + 1) * 45) && i < claims.size()) {

            Claim claim = claims.get(i);
            ItemStackBuilder builder = ItemStackBuilder.of(CLAIM_ITEM_TYPE.getOrDefault(claim.getLesserBoundaryCorner().getWorld().getEnvironment(), Material.GRASS_BLOCK));

            if (plugin.getClaimRenameMap().containsKey(claim.getID())) {
                builder.name(ChatColor.GREEN + plugin.getClaimRenameMap().get(claim.getID()));
            } else {
                builder.name(ChatColor.GREEN + "Unnamed Claim (" + count++ + ")");
            }

            Location min = claim.getLesserBoundaryCorner();
            Location max = claim.getGreaterBoundaryCorner();
            Location location = new Location(
                    min.getWorld(),
                    (min.getBlockX() + max.getBlockX()) / 2,
                    (min.getBlockY() + max.getBlockY()) / 2,
                    (min.getBlockZ() + max.getBlockZ()) / 2
            );

            List<String> claimLore = plugin.useFloodgateUI && FloodgateApi.getInstance().isFloodgatePlayer(getPlayer().getUniqueId()) ? claimLoreBedrock : claimLoreJava;

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

            if (slotIter.hasNext()) {
                setItem(slotIter.next(), builder.build(() -> {
                    // right click
                    plugin.promptPlayer(claim, getPlayer());
                }, () -> {
                    // left click
                    if (plugin.useFloodgateUI && FloodgateApi.getInstance().isFloodgatePlayer(getPlayer().getUniqueId())) {
                        getPlayer().closeInventory();
//                    new BedrockUI(plugin, claim, location, getPlayer()).open();
                        teleportPlayer(location);
                    } else {
                        teleportPlayer(location);
                    }
                }));
            }
            i++;
        }


        if (page == 0) {
            setItem(48, ItemStackBuilder.of(Material.BARRIER).name(ChatColor.RED + "No Previous Page").build(() -> {}));
        } else {
            setItem(48, ItemStackBuilder.of(Material.ARROW).name(ChatColor.RED + "Back")
                    .build(() -> new ClaimsUI(plugin, getPlayer(), "Teleport Menu", page - 1).open()));
        }

        if ((page + 1) * 45 > claims.size()) {
            setItem(50, ItemStackBuilder.of(Material.BARRIER).name(ChatColor.RED + "No Next Page").build(() -> {}));
        } else {
            setItem(50, ItemStackBuilder.of(Material.ARROW).name(ChatColor.GREEN + "Next")
                    .build(() -> new ClaimsUI(plugin, getPlayer(), "Teleport Menu", page + 1).open()));
        }


    }

    public void teleportPlayer(Location location) {



        if (location.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            for (int y = 64; y < 128; y++) {
                Block block = location.getWorld().getBlockAt(location.getBlockX(), y, location.getBlockZ());
                if (block.getType().isAir() && block.getRelative(BlockFace.UP).getType().isAir() && block.getRelative(BlockFace.DOWN).isSolid()) {
                    if (plugin.isUsingFoliaAPI()) {
                        getPlayer().teleportAsync(block.getLocation().add(0.5, 0, 0.5));
                    } else {
                        getPlayer().teleport(block.getLocation().add(0.5, 0, 0.5));
                    }
                }
            }
        }
        if (plugin.isUsingFoliaAPI()) {
            getPlayer().teleportAsync(location.toHighestLocation().add(0.5, 1, 0.5));
        } else {
            getPlayer().teleport(location.toHighestLocation().add(0.5, 1, 0.5));
        }
    }

}
