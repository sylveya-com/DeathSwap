package dev.lokspel.deathswap.game.player;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;

public final class PlayerState {

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final ItemStack[] offHand;

    private final int level;
    private final float exp;
    private final int totalExperience;

    private final double health;
    private final int foodLevel;
    private final float saturation;

    private final int fireTicks;
    private final int freezeTicks;
    private final int remainingAir;

    private final GameMode gameMode;
    private final Collection<PotionEffect> effects;

    private PlayerState(Player player) {
        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.offHand = new ItemStack[]{player.getInventory().getItemInOffHand()};

        this.level = player.getLevel();
        this.exp = player.getExp();
        this.totalExperience = player.getTotalExperience();

        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();

        this.fireTicks = player.getFireTicks();
        this.freezeTicks = player.getFreezeTicks();
        this.remainingAir = player.getRemainingAir();

        this.gameMode = player.getGameMode();
        this.effects = new ArrayList<>(player.getActivePotionEffects());
    }

    public static PlayerState capture(Player player) {
        return new PlayerState(player);
    }

    /**
     * Prepares a player for a fresh match: survival with full health, food and
     * cleared status effects. Used after {@link #capture(Player)} so the lobby
     * state can be restored later.
     */
    public static void resetForMatch(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setFireTicks(0);
        player.setRemainingAir(player.getMaximumAir());
    }

    public void restore(Player player) {
        var inventory = player.getInventory();

        inventory.setContents(this.inventory);
        inventory.setArmorContents(this.armor);
        inventory.setItemInOffHand(this.offHand[0]);

        player.setLevel(this.level);
        player.setExp(this.exp);
        player.setTotalExperience(this.totalExperience);

        var maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHealth != null ? maxHealth.getValue() : this.health;
        player.setHealth(Math.min(this.health, max));
        player.setFoodLevel(this.foodLevel);
        player.setSaturation(this.saturation);

        player.setFireTicks(this.fireTicks);
        player.setFreezeTicks(this.freezeTicks);
        player.setRemainingAir(this.remainingAir);

        player.setGameMode(this.gameMode);

        for (var effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        for (var effect : this.effects) {
            player.addPotionEffect(effect);
        }
    }
}
