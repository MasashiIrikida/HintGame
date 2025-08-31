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
    UUID uuid = player.getUniqueId(); // プレイヤーのUUIDを取得
    TimerData data = new TimerData(uuid); // タイマー情報を作成
    data.setTimerActive(true); // タイマーをアクティブに設定
    timerDataMap.put(uuid, data); // マップに保存

    Difficulty difficulty = gameStartCommand.getDifficultyLabel(player, args);

    // 1秒ごとにアイテム所持チェックを行うスケジューラーを開始
    int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      Bukkit.getLogger().info("[" + player.getName() + "] アイテム所持チェック実行");

      if (hasAllTargetItems(player, difficulty)) {
        Bukkit.getLogger().info("[" + player.getName() + "] 対象アイテム（" + Arrays.toString(difficulty.getTargetItems()) + "）を所持 → タイマー停止");

        // タイマーを非アクティブにして停止
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

    taskIds.put(uuid, taskId); // タスクID保存

    // 130秒後にスケジューラを停止する遅延タスクを登録
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      TimerData currentData = timerDataMap.get(uuid);
      if (currentData == null || !currentData.isTimerActive()) {
        System.out.println("タイマーはすでに終了しています");
        return;
      }

      currentData.setTimerActive(false); // タイマー終了を記録
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

    // 指定された UUID が taskIds マップに存在するか確認
    if (taskIds.containsKey(uuid)) {

      // UUID に対応するタスク ID を取得し、スケジューラからそのタスクをキャンセル
      Bukkit.getScheduler().cancelTask(taskIds.get(uuid));

      // キャンセルしたタスクの UUID をマップから削除して、メモリリークなどを防止
      taskIds.remove(uuid);
    }

    // 指定された UUID に対応する TimerData をマップから削除し、変数 data に格納
    TimerData data = timerDataMap.remove(uuid);

    // TimerData が存在する場合（null でない場合）に処理を実行
    if (data != null) {

      // タイマーを停止（内部的に時間計測を終了）
      data.stop();

      // 空行
      player.sendMessage("");

      // プレイヤーにアイテム取得のメッセージを送信
      player.sendMessage(ChatColor.GREEN + "アイテム取得！");

      // 経過時間とスコアを表示するメッセージを送信
      player.sendMessage("経過時間: " + data.getElapsedSeconds() + " 秒 | スコア: " + data.getScore() + " 点　| よく頑張りました！");

      // タイトルを画面中央に表示（フェードイン20, 表示100, フェードアウト20）
      player.sendTitle("ゲームクリア！", "", 20, 100, 20);

      // レベルアップ時の効果音を再生（音量10.0、ピッチ1.0）
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10.0f, 1.0f);

    }

    // データベースにスコアを保存
    sessionFactory.insert(
        new PlayerScore(
            player.getName(), // プレイヤー名
            Objects.requireNonNull(data).getScore(), // 獲得スコア
            gameStartCommand.getDifficultyLabel(player, args).toString() //難易度
        )
    );

    // 難易度が HARD で、必要アイテムをすべて所持しているか
    if (difficulty == Difficulty.HARD && hasAllTargetItems(player, difficulty)) {
      // イベント発生処理（報酬、通知、エフェクトなど）
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

    // プレイヤーのインベントリから Material を抽出
    Set<Material> inventoryMaterials = Arrays.stream(player.getInventory().getContents())
        .filter(item -> item != null && item.getType() != Material.AIR)
        .map(ItemStack::getType)
        .collect(Collectors.toSet());

    // targets を Set に変換
    Set<Material> targetSet = new HashSet<>(Arrays.asList(targets));

    // 完全一致しているか（targetSet ⊆ inventoryMaterials）
    return inventoryMaterials.containsAll(targetSet);
  }
}

