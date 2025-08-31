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

  // potionEffectのゲッター風メソッド
  public void getPotionEffect(Main plugin) { potionEffect(plugin);}

  // rewardHelmetを5秒遅らせて実行するゲッター風メソッド
  public void getRewardHelmet(Player player) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> rewardHelmet(player), 100L); // 100 tick = 約5秒
  }


  /**
   * ダイバーヘルメットを生成するメソッドです
   */
  public static ItemStack createHelmet() {

    // 鉄のヘルメットアイテムを作成
    ItemStack helmet = new ItemStack(Material.IRON_HELMET);

    // アイテムのメタデータ（名前、説明、エンチャントなど）を取得
    ItemMeta meta = helmet.getItemMeta();

    // メタデータが取得できなかった場合はそのままヘルメットを返す
    if (meta == null) return helmet;

    // アイテムの表示名を設定（AQUA色で「ダイバーヘルメット」）
    meta.setDisplayName(ChatColor.AQUA + "ダイバーヘルメット");

    // 水中呼吸（Respiration）レベル3を付与
    meta.addEnchant(Enchantment.RESPIRATION, 3, true);

    // 水中作業効率（Aqua Affinity）レベル1を付与
    meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);

    // 耐久力（Unbreaking）レベル3を付与
    meta.addEnchant(Enchantment.UNBREAKING, 3, true);

    // 水中移動速度（Depth Strider）レベル3を付与
    meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);

    // 防御力（Protection）レベル4を付与
    meta.addEnchant(Enchantment.PROTECTION, 4, true);

    // 加工したメタデータをヘルメットに適用
    helmet.setItemMeta(meta);

    // 完成したカスタムヘルメットを返す
    return helmet;
  }


  /**
   * ダイバーヘルメットを渡して、メッセージと音声を再生するメソッドです
   */
  private void rewardHelmet(Player player) {
    // プレイヤーのインベントリの9番目のスロット（インデックス8）にカスタムヘルメットをセット
    player.getInventory().setItem(8, createHelmet());

    // プレイヤーに報酬獲得のメッセージを表示
    player.sendMessage("");
    player.sendMessage("ダイバーヘルメットが手に入りました！装備してみましょう！");

    // 音声化するメッセージを作成（より詳細な説明を含む）
    String message = "ここまでの努力がむくわれて、ダイバーヘルメットが手に入りました！"
        + "ヘルメットを装備して、水の中の世界をきり開いて行ってください！";
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
            // 暗視効果を付与（10秒間）
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, true, false));

            // 速度アップ効果を付与（10秒間）
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 1, true, false));
          }
        }
      }
    }, 0L, 100L); // 100 tick = 5秒ごとにチェック
  }
}

