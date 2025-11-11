# AxRankMenu v1.0.0-deii0 - New Features

## Overview
This custom version adds two major features to AxRankMenu:
1. **Grindable Ranks** - Automatic rank progression based on playtime
2. **Persistent Effects System** - Long-duration potion effects that survive server restarts

---

## Feature 1: Grindable Ranks

### What is it?
Grindable ranks are automatically granted to players when they reach a specific playtime threshold. For example, after playing for 7 days, a player automatically receives the VETERAN rank.

### Configuration (config.yml)

```yaml
grindable-ranks:
  enabled: true
  check-interval: 60  # How often to check (in seconds)
```

### Setting Up a Grindable Rank (ranks.yml)

```yaml
VETERAN:
  rank: "VETERAN"
  server: ""
  grindable: true  # Mark as grindable
  required-playtime-days: 7  # Require 7 days of playtime
  # OR use hours instead:
  # required-playtime-hours: 168
  slot: 10
  item:
    type: "DIAMOND"
    name: "&#FFD700&lVETERAN &fRANK"
    lore:
      - " "
      - " &7- &fRequired Playtime: &#FFD7007 days"
      - " "
      - "&#FFD700&l(!) &#FFD700Automatically granted!"
  buy-actions:
    - "[MESSAGE] &#FFD700Congratulations! You received VETERAN rank!"
    - "[CONSOLE] lp user %player% parent set VETERAN"
    - "[EFFECT] SPEED 1 3"  # Speed II for 3 days
```

### Key Points:
- Set `grindable: true` to enable auto-granting
- Use either `required-playtime-days` or `required-playtime-hours`
- Players are checked every minute while online
- Playtime is tracked even when server restarts
- Once granted, ranks are permanent (tracked via LuckPerms)

### Playtime Tracking:
- Starts automatically when a player joins
- Saved every 5 minutes and on player quit
- Data stored in `plugins/AxRankMenu/playtime.yml`
- Survives server restarts

---

## Feature 2: Persistent Effects System

### What is it?
A new action type `[EFFECT]` that grants potion effects with durations measured in **days**, not ticks. These effects persist across server restarts and re-login.

### Configuration (config.yml)

```yaml
persistent-effects:
  enabled: true
  apply-on-join: true  # Apply effects when player logs in
```

### Using the [EFFECT] Action

#### Format:
```yaml
[EFFECT] <effect_type> <amplifier> <duration_days>
```

#### Examples:

```yaml
buy-actions:
  # Speed II for 3 days
  - "[EFFECT] SPEED 1 3"
  
  # Night Vision I for 7 days
  - "[EFFECT] NIGHT_VISION 0 7"
  
  # Regeneration I for 30 days
  - "[EFFECT] REGENERATION 0 30"
  
  # Strength I for essentially permanent (365 days)
  - "[EFFECT] INCREASE_DAMAGE 0 365"
```

#### Effect Types (Common):
- `SPEED` - Speed
- `SLOW` - Slowness
- `FAST_DIGGING` - Haste
- `SLOW_DIGGING` - Mining Fatigue
- `INCREASE_DAMAGE` - Strength
- `HEAL` - Instant Health
- `HARM` - Instant Damage
- `JUMP` - Jump Boost
- `CONFUSION` - Nausea
- `REGENERATION` - Regeneration
- `DAMAGE_RESISTANCE` - Resistance
- `FIRE_RESISTANCE` - Fire Resistance
- `WATER_BREATHING` - Water Breathing
- `INVISIBILITY` - Invisibility
- `BLINDNESS` - Blindness
- `NIGHT_VISION` - Night Vision
- `HUNGER` - Hunger
- `WEAKNESS` - Weakness
- `POISON` - Poison
- `WITHER` - Wither
- `HEALTH_BOOST` - Health Boost
- `ABSORPTION` - Absorption
- `SATURATION` - Saturation
- `GLOWING` - Glowing
- `LEVITATION` - Levitation
- `LUCK` - Luck
- `UNLUCK` - Bad Luck
- `SLOW_FALLING` - Slow Falling
- `CONDUIT_POWER` - Conduit Power
- `DOLPHINS_GRACE` - Dolphin's Grace
- `BAD_OMEN` - Bad Omen
- `HERO_OF_THE_VILLAGE` - Hero of the Village

#### Amplifier Levels:
- `0` = Level I
- `1` = Level II
- `2` = Level III
- etc.

### How It Works:
1. Effects are applied with maximum duration (infinite appearance)
2. System tracks actual expiration time in `plugins/AxRankMenu/effects.yml`
3. Effects are reapplied every 30 seconds to maintain them
4. Expired effects are automatically removed
5. Effects survive server restarts and player re-logins
6. Data saved every 5 minutes automatically

### Management:
- Effects stored in `plugins/AxRankMenu/effects.yml`
- Checked every 30 seconds for expiration
- Applied on player join if enabled
- Removed automatically when expired

---

## Complete Example: Grindable Rank with Effects

```yaml
LEGEND:
  rank: "LEGEND"
  server: ""
  grindable: true
  required-playtime-days: 30  # 1 month of playtime
  slot: 16
  item:
    type: "NETHER_STAR"
    name: "&#FF00FF&lLEGEND &fRANK"
    lore:
      - " "
      - " &7- &fRequired: &#FF00FF30 days playtime"
      - " "
      - "&#FF00FFʀᴇᴡᴀʀᴅs"
      - " &7- Speed II (1 year)"
      - " &7- Regeneration I (7 days)"
      - " &7- Strength I (7 days)"
      - " "
      - "&#FF00FF&l(!) &#FF00FFAuto-granted!"
  buy-actions:
    - "[MESSAGE] &#FF00FFYou are now a LEGEND!"
    - "[CONSOLE] lp user %player% parent set LEGEND"
    - "[EFFECT] SPEED 1 365"
    - "[EFFECT] REGENERATION 0 7"
    - "[EFFECT] INCREASE_DAMAGE 0 7"
```

---

## Combining with Regular Ranks

You can also use `[EFFECT]` in regular purchasable ranks:

```yaml
VIP:
  rank: "VIP"
  server: ""
  price: 1000.0
  currency: Vault
  slot: 12
  item:
    type: "LIME_BANNER"
    name: "&#00FF00&lVIP &fRANK"
    lore:
      - " "
      - " &7- Price: &#00AA00$%price%"
      - " &7- Includes Speed I for 7 days!"
  buy-actions:
    - "[MESSAGE] &#00EE00You purchased VIP rank!"
    - "[CONSOLE] lp user %player% parent set VIP"
    - "[EFFECT] SPEED 0 7"  # Speed I for 7 days
    - "[CLOSE] menu"
```

---

## Language Messages (lang.yml)

New messages added:

```yaml
grindable-rank:
  received: "&#33FF33Congratulations! You have been granted the &f%rank% &#33FF33rank for your dedication!"
  effect-received: "&#33FF33You have received %effect% (Level %level%) for %days% days!"
```

---

## Data Files

The plugin creates these new files:

1. **playtime.yml** - Stores player playtime data
   - Location: `plugins/AxRankMenu/playtime.yml`
   - Format: `<uuid>: <milliseconds>`

2. **effects.yml** - Stores active persistent effects
   - Location: `plugins/AxRankMenu/effects.yml`
   - Contains effect type, amplifier, and expiration timestamp

Both files are automatically managed and saved periodically.

---

## Commands

All existing commands work the same:
- `/rankmenu` - Open the rank GUI
- `/rankmenu reload` - Reload all configs (includes new systems)
- `/rankmenu addrank <group>` - Add a rank (now supports grindable)

---

## Permissions

All existing permissions work the same:
- `axrankmenu.use` - Open rank menu
- `axrankmenu.reload` - Reload plugin
- `axrankmenu.addrank` - Add ranks

---

## Technical Details

### Performance:
- Playtime tracked every 60 seconds
- Effects checked/applied every 30 seconds
- Auto-save every 5 minutes
- Minimal performance impact

### Compatibility:
- Works with all existing AxRankMenu features
- Compatible with LuckPerms
- Works with all economy plugins
- Folia supported

### Data Persistence:
- Playtime survives server restarts
- Effects survive server restarts
- Effects survive player logouts
- Data saved on plugin shutdown

---

## Version Info

**Version**: v1.0.0-deii0  
**Base**: AxRankMenu 1.12.0  
**Author**: deii0  
**Date**: November 10, 2025

---

## Support

For issues or questions about these custom features, please contact the server administrator or deii0.
