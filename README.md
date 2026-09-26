# DeathSwap

Last-player-standing minigame with timed position swaps between alive players.

## » About

Players join a lobby and are sent into a fresh temporary world when the match starts. At regular intervals, their positions are swapped with random alive opponents. Players must survive until they are the last one standing.

## » Features

- Configurable lobby, countdown and match length
- Timed position swaps with warning countdowns
- Configurable lives, PvP and death tracking
- Last-player-standing or time-limit win conditions
- Reusable world pool or a brand-new world per match
- Optional nether and end dimensions per match
- Per-world spawn radius and world border
- Toggleable scoreboard, actionbar and sounds
- Match-scoped chat, death and advancement messages
- Optional tab-list hiding with PacketEvents

## » Commands

| Command | Aliases | Description | Permission |
| --- | --- | --- | --- |
| `/deathswap join` | `/ds join` | Join the lobby | — |
| `/deathswap leave` | `/ds leave` | Leave the lobby or match | — |
| `/deathswap start` | `/ds start` | Force-start the game | `deathswap.start` |
| `/deathswap stop` | `/ds stop` | Stop the game | `deathswap.stop` |
| `/deathswap setlobby` | `/ds setlobby` | Set the lobby location | `deathswap.setlobby` |
| `/deathswap reload` | `/ds reload` | Reload config and messages | `deathswap.reload` |

## » Placeholders

Requires [PlaceholderAPI](https://placeholderapi.com).

The expansion is available under both `deathswap` and `ds`.

| Placeholder | Description |
| --- | --- |
| `%deathswap_state%` | Player's current state: `none`, `lobby`, `match`, or `spectator` |
| `%deathswap_deaths%` | Current death count (`0` outside a match) |
| `%deathswap_deaths_left%` | Deaths remaining before elimination |
| `%deathswap_max_deaths%` | Configured maximum deaths |
| `%deathswap_players_in_lobby%` | Players currently waiting in the lobby |
| `%deathswap_min_players%` | Minimum players required to start |
| `%deathswap_swap_interval%` | Configured swap interval in seconds |
| `%deathswap_next_swap%` | Seconds until the next swap |

## » Requirements

- **Java 25**
- **Spigot, Paper, or Folia**
- **Minecraft `1.21.11`, `26.1`, or `26.2`**
- Optional: [PacketEvents](https://github.com/retrooper/packetevents) for match player tab-list hiding
- Optional: [PlaceholderAPI](https://placeholderapi.com) for placeholders

## » Tab List Hiding

When `hide.match-players-in-tab` is enabled and PacketEvents is installed, players outside a match cannot see its participants in the tab list.

Each match only displays its own participants, while lobby players remain separate from active matches.

Match visibility also scopes chat, death, and advancement messages between concurrent matches.

> **Note:** Advancement message isolation requires Paper and is unavailable on Spigot.

## » Build

```bash
mvn clean package
