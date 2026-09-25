package dev.lokspel.deathswap.scoreboard;

import dev.lokspel.deathswap.DeathSwap;
import dev.lokspel.deathswap.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MatchScoreboard {

    private final DeathSwap plugin;
    private final Set<String> scoreboardKeys = new HashSet<>();
    private Scoreboard scoreboard;
    private Objective deathsObjective;

    public MatchScoreboard(DeathSwap plugin) {
        this.plugin = plugin;
    }

    public void init(String title) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        scoreboard = manager.getNewScoreboard();
        deathsObjective = scoreboard.registerNewObjective("deaths", Criteria.DUMMY, title);
        deathsObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void update(Map<UUID, Integer> deaths) {
        if (scoreboard == null) return;

        for (String key : scoreboardKeys) {
            scoreboard.resetScores(key);
        }
        scoreboardKeys.clear();

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(deaths.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<UUID, Integer> entry : sorted) {
            int deathCount = entry.getValue();
            if (deathCount == 0) continue;

            Player player = PlayerUtil.getOnlinePlayer(entry.getKey());
            if (player == null) continue;

            String entryName = plugin.getMainConfig().messages().get("scoreboard-entry", "player", player.getName());
            if (entryName.length() > 40) {
                entryName = entryName.substring(0, 40);
            }

            deathsObjective.getScore(entryName).setScore(deathCount);
            scoreboardKeys.add(entryName);
        }
    }

    public void apply(Set<UUID> playerUuids) {
        for (Player player : PlayerUtil.getOnlinePlayers(playerUuids)) {
            player.setScoreboard(scoreboard);
        }
    }

    public void remove(Set<UUID> playerUuids) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            Scoreboard main = manager.getMainScoreboard();
            for (Player player : PlayerUtil.getOnlinePlayers(playerUuids)) {
                player.setScoreboard(main);
            }
        }
        scoreboard = null;
        deathsObjective = null;
        scoreboardKeys.clear();
    }
}
