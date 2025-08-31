package plugin.sample.mapper.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * データベースと連動するデータオブジェクトクラスです
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerScore {

  private int id;
  private String playerName;
  private int score;
  private String difficulty;
  private LocalDateTime registeredAt;

  /**
   * 新しいスコア情報を作成する時に使用するコンストラクタです
   */
  public PlayerScore(String playerName, int score, String difficulty) {
    this.playerName = playerName;
    this.score = score;
    this.difficulty = difficulty;
  }
}