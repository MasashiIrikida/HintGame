package plugin.sample.timer;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * ゲームスタートコマンドのタイマーを定義するクラスです
 */
public class TimerData {

  @Getter
  private final UUID playerId;
  private final long startTime;
  private long endTime;
  @Getter
  private int score;

  @Setter
  @Getter
  private boolean timerActive;


  /**
   * ゲームの開始時間を記録するコンストラクタです
   */
  public TimerData(UUID playerId) {
    this.playerId = playerId;
    this.startTime = System.currentTimeMillis();
    this.timerActive = true;
  }

  /**
   * ゲームタイマーを終了するメソッドです
   */
  public void stop() {
    this.endTime = System.currentTimeMillis();
    calculateScore();
  }

  /**
   * ゲームの経過時間を取得するメソッドです
   */
  public long getElapsedSeconds() {
    return (endTime - startTime) / 1000;
  }

  /**
   * ゲームのスコアを計算するメソッドです
   */
  private void calculateScore() {

    long seconds = getElapsedSeconds();

    if (seconds <= 30) {
      score = 100;
    } else {
      int penaltyUnits = (int) ((seconds - 30) / 5);
      score = Math.max(0, 100 - penaltyUnits * 5);
    }
  }
}
