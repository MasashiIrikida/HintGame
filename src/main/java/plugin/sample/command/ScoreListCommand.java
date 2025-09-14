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


/**
 * 直近20件のスコア情報を表示するコマンドクラスです
 */
public class ScoreListCommand implements CommandExecutor {

  private final SessionFactory sessionFactory = new SessionFactory();

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      System.out.println("このコマンドはプレイヤーのみ使用できます");
      return true;
    }

    List<PlayerScore> playerScoreList = sessionFactory.selectList();

    player.sendMessage("");

    for (PlayerScore playerScore : playerScoreList) {
      player.sendMessage(
          playerScore.getId() + " | "                       // スコアID（主キー）
              + playerScore.getPlayerName() + " | "             // プレイヤー名
              + playerScore.getScore() + " | "                  // スコア
              + playerScore.getDifficulty() + " | "             // 難易度
              + playerScore.getRegisteredAt()                   // 登録日時（LocalDateTime）
              .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) // 日時を整形して表示
      );
    }
    return true;
  }
}

