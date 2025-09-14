package plugin.sample.command;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Villager;
import org.bukkit.entity.EntityType;
import plugin.sample.Main;
import plugin.sample.material.JobSiteBlock;
import plugin.sample.timer.TimerManager;


/**
 * ゲームスタートコマンドを実行するクラスです
 */
public class GameStartCommand implements CommandExecutor, Listener {


  private Main plugin;
  private TimerManager timerManager;


  public GameStartCommand(Main plugin, TimerManager timerManager) {
    this.plugin = plugin;
    this.timerManager = timerManager;
  }

  private int namedVillagerCount = 0;

  private final Set<UUID> CommandUserSet = new HashSet<>();

  public GameStartCommand() {
  }

  @Getter
  private String[] lastArgs = new String[0];


  /**
   * 宝探しゲームの難易度と獲得アイテムを紐づけるイーナムです
   */
  @Getter
  public enum Difficulty {
    // 難易度ごとに対応するアイテムを設定
    EASY(new Material[]{Material.MAP}),
    NORMAL(new Material[]{Material.MAP, Material.GLOW_ITEM_FRAME}),
    HARD(new Material[]{Material.MAP, Material.GLOW_ITEM_FRAME,
        Material.SPYGLASS}),
    NONE(new Material[]{});

    private final Material[] targetItems;

    Difficulty(Material[] targetItems) {
      this.targetItems = targetItems;
    }
  }


  /**
   * 宝探しゲームのコマンド実行メソッドです
   */
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      System.out.println("このコマンドはプレイヤーのみ使用できます");
      return true;
    }

    if (CommandUserSet.contains(player.getUniqueId())) {
      player.sendMessage("");
      player.sendMessage(ChatColor.GREEN + "このコマンドは一度しか使えません");
      player.sendMessage("サーバーを再起動して、もう一度コマンドを入力してください");
      return true;
    }

    CommandUserSet.add(player.getUniqueId());

    Difficulty difficulty = getDifficultyLabel(player, args);

    if (difficulty == Difficulty.NONE) return false;

    GetVillagerLocation result = getResult(player);

    spawnFrame(player, difficulty);

    spawnSpyglass(player, difficulty);

    this.lastArgs = args;

    return isAirSolid(player, result, args);
  }


  /**
   * スポーン位置のブロックが空気、かつ、真下のブロックが固体を判定した後に実行するメソッドです
   */
  private boolean isAirSolid(Player player, GetVillagerLocation result, String[] args) {

    if (result.blockAt().getType().isAir() && result.blockBelow().getType().isSolid()) {

      spawnVillager(player, result);

      spawnThinkingVillager(player);

      setJobSiteBlock(player);

      spawnEmerald(player);

      player.sendTitle("宝探しスタート！", "" , 20, 100, 20);
      player.sendMessage("");
      player.sendMessage("宝探しが始まりました");
      player.sendMessage("ナビゲーターを右クリックしてヒントを尋ねてください");

      timerManager.startTimer(player, args);

    } else {
      player.sendMessage("ブロックのない方向、もしくは、地面のある方向を向いて、コマンドを再実行してください");
    }
    return false;
  }


  /**
   * プレイヤーが入力したコマンド引数から難易度を判定するメソッドです
   */
  public Difficulty getDifficultyLabel(Player player, String[] args) {
    if (args.length != 1) {
      player.sendMessage("");
      player.sendMessage(ChatColor.GREEN + "実行できません。コマンド引数に難易度設定が必要です。[easy, normal, hard]");
      player.sendMessage("サーバーを再起動して、もう一度コマンドを入力してください");
      return Difficulty.NONE;
    }

    String input = args[0].toLowerCase();

    switch (input) {
      case "easy":
        return Difficulty.EASY;
      case "normal":
        return Difficulty.NORMAL;
      case "hard":
        return Difficulty.HARD;
      default:
        try {

          return Difficulty.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
          return Difficulty.NONE;
        }
    }
  }


  /**
   * プレイヤーの半径20ブロックに輝く額縁をスポーンするメソッドです
   */
  private void spawnFrame(Player player, Difficulty difficulty) {

    if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {

      World world = player.getWorld();
      Location playerLoc = player.getLocation();

      int radius = 20;
      SplittableRandom random = new SplittableRandom();
      int offsetX = random.nextInt(-radius, radius + 1);
      int offsetZ = random.nextInt(-radius, radius + 1);

      Location dropLoc = playerLoc.clone().add(offsetX, 0, offsetZ);

      int y = world.getHighestBlockYAt(dropLoc);
      dropLoc.setY(y + 1);

      ItemStack glowFrame = new ItemStack(Material.GLOW_ITEM_FRAME);
      world.dropItemNaturally(dropLoc, glowFrame);
    }
  }


  /**
   * プレイヤーの半径30ブロックに望遠鏡をスポーンするメソッドです
   */
  private void spawnSpyglass(Player player, Difficulty difficulty) {
    if (difficulty != Difficulty.HARD) return;

    Location loc = player.getLocation();
    World world = loc.getWorld();

    int x = loc.getBlockX() + new SplittableRandom().nextInt(61) - 30;
    int z = loc.getBlockZ() + new SplittableRandom().nextInt(61) - 30;
    int y = Objects.requireNonNull(world).getHighestBlockYAt(x, z) + 1;

    Location dropLoc = new Location(world, x, y, z);
    world.dropItemNaturally(dropLoc, new ItemStack(Material.SPYGLASS));
  }


  /**
   * 村人をスポーンする位置を判定するメソッドです
   */
  @NotNull
  private GetVillagerLocation getResult(Player player) {

    Vector direction = player.getLocation().getDirection().normalize();

    Location baseLocation = player.getLocation().add(direction.multiply(-5));

    Location spawnLocation = baseLocation.clone();
    spawnLocation.setY(player.getLocation().getY());

    Block blockAt = spawnLocation.getBlock();

    Block blockBelow = spawnLocation.clone().add(0, -1, 0).getBlock();
    return new GetVillagerLocation(spawnLocation, blockAt, blockBelow);
  }


  /**
   * 村人のスポーン位置に関するレコードです
   * @param spawnLocation
   * @param blockAt
   * @param blockBelow
   */
  private record GetVillagerLocation(Location spawnLocation, Block blockAt, Block blockBelow) {}


  /**
   * 就職する村人を5人スポーンするメソッドです
   */
  private void spawnVillager(Player player, GetVillagerLocation result) {
    for (int i = 0; i < 5; i++) {
      Villager villager = (Villager) player.getWorld()
          .spawnEntity(result.spawnLocation(), EntityType.VILLAGER);
      plugin.getSummonedVillagerList().add(villager);
    }
  }


  /**
   * 考える村人を2人スポーンするメソッドです
   */
  private void spawnThinkingVillager(Player player) {

    Vector direction = player.getLocation().getDirection().normalize();

    Location baseLocation = player.getLocation().add(direction.multiply(5));

    Location spawnLocation = baseLocation.clone().add(0, 2, 0);

    for (int i = 0; i < 2; i++) {
      Villager villager = (Villager) player.getWorld()
          .spawnEntity(spawnLocation, EntityType.VILLAGER);
      plugin.getSummonedVillagerList().add(villager);
      namingVillager(villager);
    }
  }


  /**
   * 考える村人2人に名前をつけるメソッドです
   */
  private void namingVillager(Villager villager) {

    switch (namedVillagerCount) {
      case 0 -> {
        villager.setCustomName("考えるナビゲーター");
        villager.setCustomNameVisible(false);
        namedVillagerCount++;

        // 20 ticks = 1秒（Minecraftは1秒間に20 ticks）
        new BukkitRunnable() {
          @Override
          public void run() {
            villager.setAI(false); // AIを無効化
          }
        }.runTaskLater(plugin, 50L); // 50 ticks後に実行
      }

      case 1 -> {
        villager.setCustomName("考えるナビゲーター");
        villager.setCustomNameVisible(false);
        namedVillagerCount++;

        new BukkitRunnable() {
          @Override
          public void run() {
            villager.setAI(false);
          }
        }.runTaskLater(plugin, 50L);
      }
    }
  }


  /**
   * 職業ブロックを5個設置するメソッドです
   */
  private void setJobSiteBlock(Player player) {

    JobSiteBlock cartographyTable = new JobSiteBlock();
    cartographyTable.spawnCartographyTable(player);
    plugin.getJobSiteBlockList().add(cartographyTable);

    JobSiteBlock composter = new JobSiteBlock();
    composter.spawnComposter(player);
    plugin.getJobSiteBlockList().add(composter);

    JobSiteBlock barrel = new JobSiteBlock();
    barrel.spawnBarrel(player);
    plugin.getJobSiteBlockList().add(barrel);

    JobSiteBlock lectern = new JobSiteBlock();
    lectern.spawnLectern(player);
    plugin.getJobSiteBlockList().add(lectern);

    JobSiteBlock smithingTable = new JobSiteBlock();
    smithingTable.spawnSmithingTable(player);
    plugin.getJobSiteBlockList().add(smithingTable);
  }


  /**
   * エメラルドをランダムに10個スポーンするメソッドです
   */
  private void spawnEmerald(Player player) {

    World world = player.getWorld();
    Location baseLoc = player.getLocation();
    SplittableRandom random = new SplittableRandom();

    int spawnedEmerald = 0;
    int attempts = 0;

    while (spawnedEmerald < 10 && attempts < 100) {
      attempts++;

      int dx = random.nextInt(21) - 10;
      int dz = random.nextInt(21) - 10;

      // プレイヤーの位置は除外
      if (dx == 0 && dz == 0) continue;

      Location spawnLoc = baseLoc.clone().add(dx, 0, dz);
      int y = world.getHighestBlockYAt(spawnLoc);
      spawnLoc.setY(y + 1);

      Block worldBlockAt = world.getBlockAt(spawnLoc.clone().subtract(0, 1, 0));
      if (!worldBlockAt.getType().isSolid()) continue;

      Block blockAtSpawn = world.getBlockAt(spawnLoc);
      if (blockAtSpawn.getType() != Material.AIR) continue;

      boolean entityExists = !world.getNearbyEntities(spawnLoc, 0.5, 1, 0.5).isEmpty();
      if (entityExists) continue;

      world.dropItemNaturally(spawnLoc, new ItemStack(Material.EMERALD));
      spawnedEmerald++;
    }
  }
}