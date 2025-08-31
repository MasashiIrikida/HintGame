package plugin.sample;

import java.util.Collections;
import java.util.List;
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

  // 右クリック時に村人がヒントを返すメソッドです
  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Entity entity = event.getRightClicked();
    Player player = event.getPlayer();

    if (entity instanceof Villager villager && villager.getProfession() == Villager.Profession.NONE) {
      String name = villager.getCustomName();
      if (name == null) name = "default";

      if (name.equals("考えるナビゲーター")) {
        String[] lastArgs = plugin.getGameStartCommand().getLastArgs();
        String difficultyKey = (lastArgs.length == 1) ? lastArgs[0].toLowerCase() : "normal";

        List<String> hintList = plugin.getConfig().getStringList("hintList." + difficultyKey);
        Collections.shuffle(hintList);
        String selectedHint = hintList.isEmpty() ? "ヒントが見つかりませんでした。" : hintList.getFirst();

        player.sendMessage(selectedHint);
      }
    }
  }
}