# DeathSwap

Last-player-standing minigame with timed position swaps between alive players.

## » About

DeathSwap is a survival minigame where players compete to be the last one standing.

Players join a lobby and are sent into a fresh temporary world when the match starts. At regular intervals, their positions are swapped with random alive opponents. Players must survive until they are the last one standing.

## » Matches

Before a match starts, players join the configured lobby and wait for the match to begin.

When the match starts:

* Players are teleported to a temporary match world
* A configurable number of lives is assigned to each player
* PvP and death tracking are enabled according to the configuration
* Players are distributed around the configured spawn radius

During the match, players are periodically swapped with random alive opponents. A warning countdown is displayed before each swap.

Players who lose all of their lives are eliminated and can spectate the remaining players.

The match ends when only one player remains or when the configured match time expires.

## » Worlds

DeathSwap supports temporary worlds for each match.

Worlds can either be:

* Created as a brand-new world for every match
* Taken from a reusable world pool

Nether and End dimensions can optionally be enabled for each match.

Each match world supports configurable:

* Spawn radius
* World border
* Nether and End dimensions

Multiple matches can run simultaneously, with each match isolated in its own world.

## » Commands

| Command               | Aliases        | Description                | Permission           |
| --------------------- | -------------- | -------------------------- | -------------------- |
| `/deathswap join`     | `/ds join`     | Join the lobby             | —                    |
| `/deathswap leave`    | `/ds leave`    | Leave the lobby or match   | —                    |
| `/deathswap start`    | `/ds start`    | Force-start the game       | `deathswap.start`    |
| `/deathswap stop`     | `/ds stop`     | Stop the game              | `deathswap.stop`     |
| `/deathswap setlobby` | `/ds setlobby` | Set the lobby location     | `deathswap.setlobby` |
| `/deathswap reload`   | `/ds reload`   | Reload config and messages | `deathswap.reload`   |

## » Tab List Hiding

When `hide.match-players-in-tab` is enabled and PacketEvents is installed, players outside a match cannot see its participants in the tab list.

Each match only displays its own participants, while lobby players remain separate from active matches.

Match visibility also scopes chat, death, and advancement messages between concurrent matches.

> **Note:** Advancement message isolation requires Paper and is unavailable on Spigot.

## » Placeholders

Requires [PlaceholderAPI](https://placeholderapi.com).

The expansion is available under both `deathswap` and `ds`.

| Placeholder                    | Description                                                      |
| ------------------------------ | ---------------------------------------------------------------- |
| `%deathswap_state%`            | Player's current state: `none`, `lobby`, `match`, or `spectator` |
| `%deathswap_deaths%`           | Current death count (`0` outside a match)                        |
| `%deathswap_deaths_left%`      | Deaths remaining before elimination                              |
| `%deathswap_max_deaths%`       | Configured maximum deaths                                        |
| `%deathswap_players_in_lobby%` | Players currently waiting in the lobby                           |
| `%deathswap_min_players%`      | Minimum players required to start                                |
| `%deathswap_swap_interval%`    | Configured swap interval in seconds                              |
| `%deathswap_next_swap%`        | Seconds until the next swap                                      |

## » Requirements

* **Java 25**
* **Spigot, Paper, or Folia**
* **Minecraft `1.21.11`, `26.1`, or `26.2`**
* Optional: [PacketEvents](https://github.com/retrooper/packetevents) for match player tab-list hiding
* Optional: [PlaceholderAPI](https://placeholderapi.com) for placeholders

## » Build

```bash
gradlew build
```

Enjoy DeathSwap!
