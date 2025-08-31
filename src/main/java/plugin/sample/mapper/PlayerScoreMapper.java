package plugin.sample.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.sample.mapper.data.PlayerScore;

/**
 * SQLの中身を定義するインターフェースです
 */
public interface PlayerScoreMapper {

  /**
   * player_scoreテーブルの全レコードを取得します
   */
  @Select("SELECT * FROM player_score")
  List<PlayerScore> selectList();

  /**
   * player_scoreテーブルのスコア上位5件を取得します
   */
  @Select("""
      SELECT * FROM player_score
          ORDER BY score DESC
          LIMIT 5
      """)
  List<PlayerScore> selectTop5();

  /**
   * player_scoreテーブルの最新1件を取得します
   */
  @Select("""
      SELECT * FROM player_score
      ORDER BY registered_at DESC
      LIMIT 1
  """)
  PlayerScore selectLatest();

  /**
   * 新しいスコア情報を player_scoreテーブルに挿入します
   */
  @Insert("INSERT INTO player_score(player_name, score, difficulty, registered_at) " +
      "VALUES (#{playerName}, #{score}, #{difficulty}, NOW())")
  void insert(PlayerScore playerScore);
}

