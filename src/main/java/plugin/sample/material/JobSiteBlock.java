package plugin.sample.material;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * 職業ブロックに関するクラスです
 */
public class JobSiteBlock {

  @Getter
  private Location spawnLocation;

  private record Result(Block blockAt, Block blockBelow) {}

  /**
   * 製図台をスポーンするメソッドです
   */
  public void spawnCartographyTable(Player player) {

    // プレイヤーの背面7ブロック先の位置を取得
    Location loc = player.getLocation();
    Vector direction = loc.getDirection().normalize();
    Location baseLocation = loc.add(direction.multiply(-7));

    // プライベートメソッドgetBlockを使用
    Result result = getBlock(player, baseLocation);

    // スポーン位置のブロックが空気、かつ、真下のブロックが固体なら、製図台と製図家をスポーン
    if (result.blockAt().getType().isAir() && result.blockBelow().getType().isSolid()) {
      result.blockAt().setType(Material.CARTOGRAPHY_TABLE);
    }
  }

  /**
   * コンポスターをスポーンするメソッドです
   */
  public void spawnComposter(Player player) {

    // プレイヤーの南西7ブロック先の位置を取得
    Location loc = player.getLocation();
    Vector forward = loc.getDirection().normalize();
    Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
    Vector northeast = forward.add(right).normalize().multiply(-7);
    Location baseLocation = loc.add(northeast);

    // プライベートメソッドgetBlockを使用
    Result result = getBlock(player, baseLocation);

    // スポーン位置のブロックが空気、かつ、真下のブロックが固体なら、コンポスターをスポーン
    if (result.blockAt.getType().isAir() && result.blockBelow.getType().isSolid()) {
      result.blockAt.setType(Material.COMPOSTER);
    }
  }

  /**
   * 樽をスポーンするメソッドです
   */
  public void spawnBarrel(Player player) {

    // プレイヤーの南西3ブロック先の位置を取得
    Location loc = player.getLocation();
    Vector forward = loc.getDirection().normalize();
    Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
    Vector northeast = forward.add(right).normalize().multiply(-3);
    Location baseLocation = loc.add(northeast);

    // プライベートメソッドgetBlockを使用
    Result result = getBlock(player, baseLocation);

    // スポーン位置のブロックが空気、かつ、真下のブロックが固体なら、樽をスポーン
    if (result.blockAt().getType().isAir() && result.blockBelow().getType().isSolid()) {
      result.blockAt().setType(Material.BARREL);
    }
  }

  /**
   * 書見台をスポーンするメソッドです
   */
  public void spawnLectern(Player player) {

    // プレイヤーの南東7ブロック先の位置を取得
    Location loc = player.getLocation();
    Vector forward = loc.getDirection().normalize();
    Vector left = new Vector(forward.getZ(), 0, -forward.getX()).normalize();
    Vector northwest = forward.add(left).normalize().multiply(-7);
    Location baseLocation = loc.add(northwest);

    // プライベートメソッドgetBlockを使用
    Result result = getBlock(player, baseLocation);

    // スポーン位置のブロックが空気、かつ、真下のブロックが固体なら、書見台と本をスポーン
    if (result.blockAt().getType().isAir() && result.blockBelow().getType().isSolid()) {
      Block block = result.blockAt();
      block.setType(Material.LECTERN);
    }
  }

  /**
   * 鍛冶台をスポーンするメソッドです
   */
  public void spawnSmithingTable(Player player) {

    // プレイヤーの南東3ブロック先の位置を取得
    Location loc = player.getLocation();
    Vector forward = loc.getDirection().normalize();
    Vector left = new Vector(forward.getZ(), 0, -forward.getX()).normalize();
    Vector northwest = forward.add(left).normalize().multiply(-3);
    Location baseLocation = loc.add(northwest);

    // プライベートメソッドgetBlockを使用
    Result result = getBlock(player, baseLocation);

    // スポーン位置のブロックが空気、かつ、真下のブロックが固体なら、鍛冶台をスポーン
    if (result.blockAt().getType().isAir() && result.blockBelow().getType().isSolid()) {
      result.blockAt().setType(Material.SMITHING_TABLE);
    }
  }

  /**
   * 職業ブロックのスポーン位置を取得するプライベートメソッドです
   */
  @NotNull
  private Result getBlock(Player player, Location baseLocation) {

    // 職業ブロックのスポーン位置をプレイヤーと同じ高さに固定
    this.spawnLocation = baseLocation.clone();
    spawnLocation.setY(player.getLocation().getY());

    // スポーン位置のブロックを設定
    Block blockAt = spawnLocation.getBlock();

    // スポーン位置の真下のブロックを設定
    Block blockBelow = spawnLocation.clone().add(0, -1, 0).getBlock();
    return new Result(blockAt, blockBelow);
  }
}
