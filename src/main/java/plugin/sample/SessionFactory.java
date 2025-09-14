package plugin.sample;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.sample.mapper.PlayerScoreMapper;
import plugin.sample.mapper.data.PlayerScore;

/**
 * MyBatisを通じて、データベースと接続するクラスです
 */
public class SessionFactory {

  private SqlSessionFactory sqlSessionFactory;

  private PlayerScoreMapper mapper;

  /**
   * データベースと接続するメソッドです
   */
  public SessionFactory() {
    try {

      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");

      this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

      SqlSession session = sqlSessionFactory.openSession(true);

      this.mapper = session.getMapper(PlayerScoreMapper.class);
    } catch (Exception e) {

      throw new RuntimeException(e);
    }
  }


  /**
   * データベースのテーブルから全スコア情報を取得します。
   */
  public List<PlayerScore> selectList() {
    return mapper.selectList();
  }


  /**
   * データベースのテーブルからスコア上位5件を取得します
   */
  public List<PlayerScore> selectTop5() {
    return mapper.selectTop5();
  }


  /**
   * データベースのテーブルから最新の1件を取得します
   */
  public PlayerScore selectLatest() {
    return mapper.selectLatest();
  }


  /**
   * データベースのテーブルに新しいスコア情報を登録します
   */
  public void insert(PlayerScore playerScore) {
    mapper.insert(playerScore);
  }
}

