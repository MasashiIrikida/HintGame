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
    calculateScore(); // 経過時間に応じてスコアを算出
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
    // 経過時間（秒）を取得する
    long seconds = getElapsedSeconds(); // 例: 55秒なら55が返る

    // 経過時間が30秒以下なら満点
    if (seconds <= 30) {
      score = 100; // 30秒以内ならスコアは100点
    } else {
      // 30秒を超えていた場合、5秒ごとに5点減点する
      int penaltyUnits = (int) ((seconds - 30) / 5); // 減点回数を計算

      // 減点後のスコアを計算し、最低でも0点になるようにする
      // 例: 55秒なら (55 - 30) / 5 = 5 → 25点減点
      score = Math.max(0, 100 - penaltyUnits * 5); // 例: 100 - 25 = 75点
    }
  }
}
