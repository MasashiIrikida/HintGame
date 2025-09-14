package plugin.sample.timer;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.sample.Main;
import plugin.sample.SessionFactory;
import plugin.sample.command.GameStartCommand;
import plugin.sample.command.GameStartCommand.Difficulty;
import plugin.sample.item.DiverHelmet;
import plugin.sample.mapper.data.PlayerScore;


/**
 * タイマーのふるまいを規定するクラスです
 */
public class TimerManager {

  private final Main plugin;
  public TimerManager(Main plugin) {
    this.plugin = plugin;
  }

  private final SessionFactory sessionFactory = new SessionFactory();
  private final GameStartCommand gameStartCommand = new GameStartCommand();

  @Getter
  private final Map<UUID, TimerData> timerDataMap = new HashMap<>();

  @Getter
  private final Map<UUID, Integer> taskIds = new HashMap<>();


  /**
   * タイマー計測を開始するメソッドです
   */
  public void startTimer(Player player, String[] args) {
    UUID uuid = player.getUniqueId();
    TimerData data = new TimerData(uuid);
    data.setTimerActive(true);
    timerDataMap.put(uuid, data);

    Difficulty difficulty = gameStartCommand.getDifficultyLabel(player, args);

    int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      Bukkit.getLogger().info("[" + player.getName() + "] アイテム所持チェック実行");

      if (hasAllTargetItems(player, difficulty)) {
        Bukkit.getLogger().info("[" + player.getName() + "] 対象アイテム（" + Arrays.toString(difficulty.getTargetItems()) + "）を所持 → タイマー停止");

        TimerData currentData = timerDataMap.get(uuid);
        if (currentData != null) {
          currentData.setTimerActive(false);
          Bukkit.getScheduler().cancelTask(taskIds.get(uuid));
        }

        stopGame(player, args, difficulty);
      } else {
        Bukkit.getLogger().info("[" + player.getName() + "] 対象アイテム未所持");
      }
    }, 0L, 20L).getTaskId();

    taskIds.put(uuid, taskId);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      TimerData currentData = timerDataMap.get(uuid);
      if (currentData == null || !currentData.isTimerActive()) {
        System.out.println("タイマーはすでに終了しています");
        return;
      }

      currentData.setTimerActive(false);
      Bukkit.getScheduler().cancelTask(taskId);
      Bukkit.getLogger().info("[" + player.getName() + "] タイマーが130秒経過したため自動停止");
      player.sendMessage("");
      player.sendMessage(ChatColor.GREEN + "制限時間内にゲームをクリアできませんでした");
      player.sendMessage("サーバーを再起動して、もう一度チャレンジしてみてください");
    }, 130 * 20L);
  }


  /**
   * タイマー計測終了、データベースにスコア登録、ダイバーヘルメットを授与するメソッドです
   */
  public void stopGame(Player player, String[] args, Difficulty difficulty) {
    UUID uuid = player.getUniqueId();

    if (taskIds.containsKey(uuid)) {

      Bukkit.getScheduler().cancelTask(taskIds.get(uuid));

      taskIds.remove(uuid);
    }

    TimerData data = timerDataMap.remove(uuid);

    if (data != null) {

      data.stop();

      player.sendMessage("");

      player.sendMessage(ChatColor.GREEN + "アイテム取得！");

      player.sendMessage("経過時間: " + data.getElapsedSeconds() + " 秒 | スコア: " + data.getScore() + " 点 | よく頑張りました！");

      player.sendTitle("ゲームクリア！", "", 20, 100, 20);

      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10.0f, 1.0f);

    }

    sessionFactory.insert(
        new PlayerScore(
            player.getName(),
            Objects.requireNonNull(data).getScore(),
            gameStartCommand.getDifficultyLabel(player, args).toString()
        )
    );

    if (difficulty == Difficulty.HARD && hasAllTargetItems(player, difficulty)) {
      DiverHelmet diverHelmet = new DiverHelmet(plugin);
      diverHelmet.getRewardHelmet(player);
    }
  }


  /**
   * 難易度別の獲得アイテムを全て持っているか判定するメソッドです
   */
  private boolean hasAllTargetItems(Player player, Difficulty difficulty) {
    Material[] targets = difficulty.getTargetItems();
    if (targets == null || targets.length == 0) return false;

    Set<Material> inventoryMaterials = Arrays.stream(player.getInventory().getContents())
        .filter(item -> item != null && item.getType() != Material.AIR)
        .map(ItemStack::getType)
        .collect(Collectors.toSet());

    Set<Material> targetSet = new HashSet<>(Arrays.asList(targets));

    return inventoryMaterials.containsAll(targetSet);
  }
}
