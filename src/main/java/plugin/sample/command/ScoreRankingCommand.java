package plugin.sample.command;


import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.sample.SessionFactory;
import plugin.sample.mapper.data.PlayerScore;


public class ScoreRankingCommand implements CommandExecutor {


  private final SessionFactory sessionFactory = new SessionFactory();


  /**
   * 上位5位のスコアランキングと最新のスコア情報を表示するコマンドクラスです
   */
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      System.out.println("このコマンドはプレイヤーのみ使用できます");
      return true;
    }

    // データベースから上位5件のプレイヤースコアを取得
    List<PlayerScore> playerScoreTop5 = sessionFactory.selectTop5();

    // 空行
    player.sendMessage("");

    // 各スコア情報をプレイヤーにチャットで表示
    player.sendMessage(ChatColor.GREEN + "スコアランキング上位5件");

    for (int i = 0; i < playerScoreTop5.size(); i++) {
      PlayerScore ps = playerScoreTop5.get(i);
      int rank = i + 1; // インデックスは0から始まるので+1

      player.sendMessage("第" + rank + "位 | "
          + ps.getPlayerName() + " | "
          + ps.getScore() + " | "
          + ps.getDifficulty() + " | "
          + ps.getRegisteredAt()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      );
    }

    // 空行
    player.sendMessage("");

    // データベースから直近のプレイヤースコアを取得
    PlayerScore playerScoreLatest = sessionFactory.selectLatest();

    // スコア情報をプレイヤーにチャットで表示
    player.sendMessage(ChatColor.GREEN + "最新スコア情報");
    player.sendMessage(playerScoreLatest.getPlayerName() + " | "  // プレイヤー名
        + playerScoreLatest.getScore() + " | "                  // スコア
        + playerScoreLatest.getDifficulty() + " | "             // 難易度
        + playerScoreLatest.getRegisteredAt()                   // 登録日時（LocalDateTime）
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) // 日時を整形して表示
    );
    return true;
  }
}