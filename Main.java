package plugin.sample;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.sample.command.GameStartCommand;
import plugin.sample.command.ScoreListCommand;
import org.bukkit.entity.Villager;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.event.player.PlayerQuitEvent;
import plugin.sample.command.ScoreRankingCommand;
import plugin.sample.item.DiverHelmet;
import plugin.sample.material.JobSiteBlock;
import plugin.sample.timer.TimerManager;


/**
 * メインクラスです
 */
public final class Main extends JavaPlugin implements Listener {

  // 村人をリストに追加するゲッターとそのリスト
  @Getter
  private final List<Villager> summonedVillagerList = new ArrayList<>();

  // 職業ブロックをリストに追加するゲッターとそのリスト
  @Getter
  private final List<JobSiteBlock> jobSiteBlockList = new ArrayList<>();

  // gameStartCommandをパッケージ全体で用いるためのゲッターと変数
  @Getter
  private GameStartCommand gameStartCommand;

  // カスタムBGMを登録
  private final Map<UUID, BukkitTask> musicTasks = new HashMap<>();


  /**
   * サーバー起動時に実行するメソッドです
   */
  @Override
  public void onEnable() {
    saveDefaultConfig();

    // ダイバーヘルメットにポーション効果を付与
    DiverHelmet diverHelmet = new DiverHelmet(this);
    diverHelmet.getPotionEffect(this);

    TimerManager timerManager = new TimerManager(this);

    // GameStartCommand を先に初期化
    this.gameStartCommand = new GameStartCommand(this, timerManager);

    // コマンド登録
    Objects.requireNonNull(getCommand("gameStart")).setExecutor(gameStartCommand);
    Objects.requireNonNull(getCommand("scoreList")).setExecutor(new ScoreListCommand());
    Objects.requireNonNull(getCommand("scoreRanking")).setExecutor(new ScoreRankingCommand());

    // リスナー登録
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new VillagerRespond(this), this);
  }


  /**
   * ログイン時に実行するメソッドです
   */
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.getPlayer().sendTitle("Welcome", "to Minecraft", 10, 3610, 10);
    event.getPlayer().sendMessage("/gamestart [easy, normal, hard] コマンドで,宝探しゲームを実行できます！");
  }


  /**
   * ログアウト時に実行するメソッドです
   */
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    // コマンドから呼び出した村人をゲームから削除
    summonedVillagerList.stream()
        .filter(villager -> villager != null && !villager.isDead())
        .forEach(Entity::remove);
    summonedVillagerList.clear();

    // コマンドから呼び出した職業ブロックをゲームから削除
    jobSiteBlockList.stream()
        .filter(jobSiteBlock -> jobSiteBlock != null && jobSiteBlock.getSpawnLocation() != null)
        .forEach(jobSiteBlock -> jobSiteBlock.getSpawnLocation().getBlock().setType(Material.AIR));
    jobSiteBlockList.clear();

    // ワールドからアイテムを削除
    removeWorldItems(event);

    // インベントリからアイテムを削除
    removeInventoryItems(player);

    System.out.println(player.getName() + "がログアウトしました");
  }


  /**
   * ワールドから生成アイテムを削除するメソッドです
   */
  private static void removeWorldItems(PlayerQuitEvent event) {
    for (Entity entity : event.getPlayer().getWorld().getEntities()) {
      if (entity instanceof Item item) {
        ItemStack stack = item.getItemStack();
        Material type = stack.getType();

        if (type == Material.EMERALD ||         // エメラルド
            type == Material.GLOW_ITEM_FRAME || // 輝く額縁
            type == Material.MAP ||             // 白地図
            type == Material.FILLED_MAP ||      // 地図
            type == Material.SPYGLASS) {        // 望遠鏡
          item.remove();
        }
      }
    }
  }


  /**
   * プレイヤーのインベントリから獲得アイテムを削除するメソッドです
   */
  public void removeInventoryItems(Player player) {
    Inventory inventory = player.getInventory();

    List<Material> targetMaterials = Arrays.asList(
        Material.MAP,               // 白地図
        Material.FILLED_MAP,        // 地図
        Material.GLOW_ITEM_FRAME,   // 輝く額縁
        Material.SPYGLASS,          // 望遠鏡
        Material.EMERALD            // エメラルド
    );

    for (ItemStack item : inventory.getContents()) {
      if (item != null && targetMaterials.contains(item.getType())) {
        inventory.remove(item);
      }
    }
  }
}