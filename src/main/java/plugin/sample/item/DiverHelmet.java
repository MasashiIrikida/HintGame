package plugin.sample.item;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugin.sample.Main;


/**
 * ダイバーヘルメット（特殊効果付きのヘルメット）を規定するクラスです
 */
public class DiverHelmet implements Listener {

  private final Main plugin;

  public DiverHelmet(Main plugin) {
    this.plugin = plugin;
  }

  public void getPotionEffect(Main plugin) { potionEffect(plugin);}

  public void getRewardHelmet(Player player) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> rewardHelmet(player), 100L); // 100 tick = 約5秒
  }


  /**
   * ダイバーヘルメットを生成するメソッドです
   */
  public static ItemStack createHelmet() {

    ItemStack helmet = new ItemStack(Material.IRON_HELMET);

    ItemMeta meta = helmet.getItemMeta();

    if (meta == null) return helmet;

    meta.setDisplayName(ChatColor.AQUA + "ダイバーヘルメット");

    meta.addEnchant(Enchantment.RESPIRATION, 3, true);

    meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);

    meta.addEnchant(Enchantment.UNBREAKING, 3, true);

    meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);

    meta.addEnchant(Enchantment.PROTECTION, 4, true);

    helmet.setItemMeta(meta);

    return helmet;
  }


  /**
   * ダイバーヘルメットを渡して、メッセージと音声を再生するメソッドです
   */
  private void rewardHelmet(Player player) {

    player.getInventory().setItem(8, createHelmet());

    player.sendMessage("");
    player.sendMessage("ここまでの努力がむくわれて、ダイバーヘルメットが手に入りました！"
        + "\nヘルメットを装備して、水の中の世界をきり開いて行ってください！");
  }


  /**
   * ヘルメット装着時に暗視効果と速度アップ効果を付与するメソッドです
   */
  private void potionEffect(JavaPlugin plugin) {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (Player player : Bukkit.getOnlinePlayers()) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.hasItemMeta()) {
          ItemMeta meta = helmet.getItemMeta();
          if (meta != null && ChatColor.stripColor(meta.getDisplayName()).equals("ダイバーヘルメット")) {

            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, true, false));

            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 1, true, false));
          }
        }
      }
    }, 0L, 100L); // 100 tick = 5秒ごとにチェック
  }
}