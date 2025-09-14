package plugin.sample;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * 村人との会話でヒントを返すクラスです
 */
public class VillagerRespond implements Listener {

  private final Main plugin;

  public VillagerRespond(Main plugin) {
    this.plugin = plugin;
  }

  /**
   * 村人に名前を与え、かつ、右クリックした時にヒントを返すメソッドです
   */
  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Entity entity = event.getRightClicked();
    Player player = event.getPlayer();

    if (entity instanceof Villager villager && villager.getProfession() == Villager.Profession.NONE) {
      String name = villager.getCustomName();

      if (Objects.requireNonNull(name).equals("考えるナビゲーター")) {
        String[] lastArgs = plugin.getGameStartCommand().getLastArgs();
        String difficultyKey = (lastArgs.length == 1) ? lastArgs[0].toLowerCase() : "";

        List<String> hintList = plugin.getConfig().getStringList("hintList." + difficultyKey);
        Collections.shuffle(hintList);
        String selectedHint = hintList.isEmpty() ? "ヒントが見つかりませんでした。" : hintList.get(0);
        player.sendMessage(selectedHint);
      }
    }
  }
}