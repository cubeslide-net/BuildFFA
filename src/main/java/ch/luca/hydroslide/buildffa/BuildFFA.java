package ch.luca.hydroslide.buildffa;

import ch.luca.hydroslide.buildffa.commands.Build;
import ch.luca.hydroslide.buildffa.commands.Inventory;
import ch.luca.hydroslide.buildffa.commands.Ranking;
import ch.luca.hydroslide.buildffa.commands.Setup;
import ch.luca.hydroslide.buildffa.commands.Stats;
import ch.luca.hydroslide.buildffa.commands.Teleport;
import ch.luca.hydroslide.buildffa.commands.Vanish;
import ch.luca.hydroslide.buildffa.database.BuildFFADatabase;
import ch.luca.hydroslide.buildffa.item.ItemManager;
import ch.luca.hydroslide.buildffa.user.UserManager;
import ch.luca.hydroslide.buildffa.util.LocationUtil;
import com.google.common.reflect.ClassPath;
import fr.mrmicky.fastboard.FastBoard;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

@Getter
public class BuildFFA extends JavaPlugin {

  @Getter
  private static BuildFFA instance;
  @Getter
  private final List<Player> allowBuild = new ArrayList<>();
  @Getter
  private final Map<Location, Long> blockDestroy = new HashMap<>();
  private final String prefix = "§5BuildFFA §8» §7";
  private final String prefixUse = "§5BuildFFA §8» §cBenutze: §e/";
  private final String noPlayer = "§5BuildFFA §8» §cBenutze diesen Befehl bitte im Spiel.";
  private final String playerNotOnline = "§5BuildFFA §8» §cDieser Spieler ist nicht online.";
  private final String playerNeverOnline = "§5BuildFFA §8» §cDieser Spieler wurde in der Datenbank nicht gefunden.";
  private final String noPermission = "§5BuildFFA §8» §cFür diesen Befehl fehlt dir die Berechtigung.";
  @Getter
  private BasicDataSource sqlPool;
  @Getter
  private BuildFFADatabase buildFFADatabase;
  @Getter
  private UserManager userManager;
  @Getter
  private ItemManager itemManager;
  @Getter
  private Scoreboard scoreboard;
  @Getter
  @Setter
  private Location spawnLocation;
  @Getter
  @Setter
  private int fightHeight = 0, deathHeight = 0;
  private HashMap<UUID, FastBoard> boards;

  @Override
  public void onEnable() {
    instance = this;
    try {
      if (!getDataFolder().exists()) {
        getDataFolder().mkdir();
      }
      File configFile = new File(getDataFolder().getPath(), "config.yml");
      if (!configFile.exists()) {
        configFile.createNewFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
        cfg.set("MySQL.Host", "host");
        cfg.set("MySQL.Database", "db");
        cfg.set("MySQL.Password", "pw");
        cfg.set("MySQL.User", "user");
        cfg.set("MySQL.Port", 3306);
        cfg.save(configFile);
      } else {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
        if (cfg.get("Spawn") != null) {
          this.spawnLocation = LocationUtil.stringToLocation(cfg.getString("Spawn"));
        }
        if (cfg.get("FightHeight") != null) {
          this.fightHeight = cfg.getInt("FightHeight");
        }
        if (cfg.get("DeathHeight") != null) {
          this.deathHeight = cfg.getInt("DeathHeight");
        }
        try {
          sqlPool = new BasicDataSource();
          sqlPool.setDriverClassName("com.mysql.jdbc.Driver");
          sqlPool.setUrl(
              "jdbc:mysql://" + cfg.getString("MySQL.Host") + ":" + cfg.getInt("MySQL.Port") + "/"
                  + cfg.getString("MySQL.Database"));
          sqlPool.setUsername(cfg.getString("MySQL.User"));
          sqlPool.setPassword(cfg.getString("MySQL.Password"));
          sqlPool.setMaxIdle(30);
          sqlPool.setMinIdle(5);
          sqlPool.setDriverClassLoader(BuildFFA.class.getClassLoader());
          System.out.println("[BuildFFA] MySQL connected!");
          Connection connection = null;
          try {
            connection = sqlPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS BuildFFA (uuid VARCHAR(100), name VARCHAR(100), kills INT(100), deaths INT(100), coins INT(100), inventory LONGTEXT, PRIMARY KEY(uuid))");
            preparedStatement.executeUpdate();
            preparedStatement.close();
          } catch (SQLException e) {
            e.printStackTrace();
          } finally {
            try {
              if (connection != null) {
                connection.close();
              }
            } catch (Exception exc) {
              exc.printStackTrace();
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.buildFFADatabase = new BuildFFADatabase();
    this.userManager = new UserManager();
    this.itemManager = new ItemManager();

    PluginManager pluginManager = Bukkit.getPluginManager();
    getCommand("build").setExecutor(new Build(this));
    getCommand("setup").setExecutor(new Setup(this));
    getCommand("inventory").setExecutor(new Inventory(this));
    getCommand("ranking").setExecutor(new Ranking(this));
    getCommand("stats").setExecutor(new Stats(this));
    getCommand("vanish").setExecutor(new Vanish(this));
    getCommand("teleport").setExecutor(new Teleport(this));
    pluginManager.registerEvents(new Vanish(this), this);

    this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    this.boards = new HashMap<>();
    try {
      for (ClassPath.ClassInfo classInfo : ClassPath.from(getClassLoader())
          .getTopLevelClasses("ch.luca.hydroslide.buildffa.listener")) {
        @SuppressWarnings("rawtypes")
        Class clazz = Class.forName(classInfo.getName());
        if (Listener.class.isAssignableFrom(clazz)) {
          pluginManager.registerEvents((Listener) clazz.newInstance(), this);
        }
      }
    } catch (IOException | ClassNotFoundException | IllegalAccessException |
             InstantiationException e) {
      e.printStackTrace();
    }

    new BukkitRunnable() {
        @Override
        public void run() {
            for(Player player : Bukkit.getOnlinePlayers()) {
                sendScoreboard(player);
            }
        }
    }.runTaskTimer(getInstance(), 20, 20);

    Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
      long current = System.currentTimeMillis();
      Iterator<Entry<Location, Long>> iterator = this.blockDestroy.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<Location, Long> entry = iterator.next();
        Location loc = entry.getKey();
        long time = entry.getValue();
        if (loc.getBlock().getType().equals(Material.SANDSTONE) && current >= time + 5000) {
          loc.getBlock().setType(Material.RED_SANDSTONE);
        } else if (current >= time + 10000) {
          loc.getBlock().setType(Material.AIR);
          iterator.remove();
        }
      }
    }, 1L, 1L);
  }

  @Override
  public void onDisable() {
      if (sqlPool == null || sqlPool.isClosed()) {
          return;
      }
    try {
      sqlPool.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void sendScoreboard(Player player) {
    final UUID uuid = player.getUniqueId();

    FastBoard board;
    if (!boards.containsKey(uuid)) {
      board = new FastBoard(player);
      board.updateTitle("§bCube§3Slide §a✧ §5BuildFFA");
      boards.put(uuid, board);
    } else {
      board = boards.get(uuid);
    }

    BuildFFA.getInstance().getBuildFFADatabase()
        .executeQuery("SELECT * FROM BuildFFA WHERE name='" + player.getName() + "'", true,
            rs -> {
              if (rs == null) {
                return;
              }
              try {
                if (rs.next()) {
                  int rank = BuildFFA.getInstance().getBuildFFADatabase()
                      .getRank(rs.getString("UUID"));
                  List<String> boardText = Arrays.asList("§5", "§a✧ §7Kills",
                      "  §b➼ §a" + rs.getInt("kills"), "§d", "§a✧ §7Deaths",
                      "  §b➼ §c" + rs.getInt("deaths"), "§d", "§a✧ §7Rank", "  §b➼ §c" + rank);
                  board.updateLines(boardText);
                }
              } catch (SQLException e) {

              }
            });

  }


    public HashMap<UUID, FastBoard> getBoards() {
        return boards;
    }
}