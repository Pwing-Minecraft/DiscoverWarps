package me.eccentric_nz.discoverwarps.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Class for constructing items from config
 *
 * Created by Redned on 6/26/2019.
 */
public class ItemUtil {

    public static ItemStack fromString(String str) {
        if (str == null || str.isEmpty())
            return null;

        String string = str;
        Material mat;
        String name = null;

        if (string.contains("|")) {
            String[] temp = string.split("\\|");
            string = temp[0];

            name = ChatColor.translateAlternateColorCodes('&', temp[1]);
        }

        mat = Material.matchMaterial(string.toUpperCase());

        if (mat == null)
            mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        if (name != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack readItemFromConfig(String configPath, FileConfiguration config) {
        ItemStack stack = new ItemStack(Material.STONE);

        if (!config.contains(configPath))
            return null;

        ItemMeta meta = stack.getItemMeta();
        for (String str : config.getConfigurationSection(configPath).getKeys(false)) {
            switch (str) {
                case "type":
                case "material":
                case "item":
                    stack = new ItemStack(Material.matchMaterial(config.getString(configPath + "." + str).toUpperCase()));
                    break;
                case "durability":
                case "data":
                    stack.setDurability((short) config.getInt(configPath + "." + str));
                    break;
                case "custom-model-data":
                case "model-data":
                    meta.setCustomModelData(config.getInt(configPath + "." + str));
                    break;
                case "amount":
                    stack.setAmount(config.getInt(configPath + "." + str));
                    break;
                case "name":
                case "display-name":
                    meta.setDisplayName(config.getString(configPath + "." + str));
                    break;
                case "enchants":
                case "enchantments":
                    for (String enchant : config.getStringList(configPath + "." + str)) {
                        String[] split = enchant.split(" ");
                        int level = Integer.parseInt(split[1]);

                        if (!isEnchantment(split[0]))
                            break;

                        Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(split[0].toLowerCase()));
                        meta.addEnchant(enchantment, level, true);
                    }
                    break;
                case "lore":
                    meta.setLore(config.getStringList(configPath + "." + str));
                    break;
                case "unbreakable":
                     meta.setUnbreakable(config.getBoolean(configPath + "." + str));
                    break;
                case "owner":
                case "head-owner":
                    if (meta instanceof SkullMeta) {
                        SkullMeta skullMeta = (SkullMeta) meta;
                        skullMeta.setOwner(config.getString(configPath + "." + str));
                    }
                    break;
                case "color":
                case "colour":
                    String[] colorSplit = config.getString(configPath + "." + str).split(",");
                    Color color = null;

                    if (colorSplit.length == 3)
                        color = Color.fromRGB(Integer.parseInt(colorSplit[0]), Integer.parseInt(colorSplit[1]), Integer.parseInt(colorSplit[2]));
                    else
                        color = fromHex(config.getString(configPath + "." + str));

                    if (color != null) {
                        if (meta instanceof PotionMeta) {
                            PotionMeta potionMeta = (PotionMeta) meta;
                            potionMeta.setColor(color);
                        }
                        if (meta instanceof LeatherArmorMeta) {
                            LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
                            armorMeta.setColor(color);
                        }
                    }
                    break;
                case "item-flags":
                    for (String flag : config.getStringList(configPath + "." + str)) {
                        if (!isItemFlag(flag))
                            continue;

                        meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
                    }
                    break;
                case "effects":
                case "potion-effects":
                    for (String effect : config.getStringList(configPath + "." + str)) {
                        String[] effectSplit = effect.split(" ");
                        PotionEffectType effectType = PotionEffectType.getByName(effectSplit[0]);
                        if (effectType == null)
                            continue;

                        int duration = duration = Integer.parseInt(effectSplit[1]) * 20;
                        int amplifier =  amplifier = Integer.parseInt(effectSplit[2]) - 1;

                        if (meta instanceof PotionMeta) {
                            PotionMeta potionMeta = (PotionMeta) meta;
                            potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
                        }
                    }
                    break;
                default:
                    break;
            }

            // TODO: Add item attribute API
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private static Color fromHex(String hex) {
        java.awt.Color jColor = java.awt.Color.decode(hex);
        return Color.fromRGB(jColor.getRed(), jColor.getGreen(), jColor.getBlue());
    }

    public static boolean isEnchantment(String str) {
        return EnchantmentWrapper.getByKey(NamespacedKey.minecraft(str.toLowerCase())) != null;
    }

    public static boolean isItemFlag(String str) {
        try {
            ItemFlag.valueOf(str.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {/* do nothing */}

        return false;
    }
}