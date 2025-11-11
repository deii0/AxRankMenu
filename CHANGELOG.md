# AxRankMenu Changelog

## [1.0.0-deii0] - 2025-11-10

### 🎉 Major Features Added

#### Grindable Ranks System
- **Automatic rank progression** based on total playtime
- Players receive ranks automatically when meeting playtime thresholds
- Support for both day-based and hour-based requirements
- Configurable check intervals (default: 60 seconds)
- Persistent playtime tracking across server restarts
- Integration with LuckPerms for rank management

#### Persistent Effects System
- **New `[EFFECT]` action type** for buy-actions
- Long-duration potion effects (measured in days, not ticks)
- Effects survive server restarts and player logouts
- Automatic expiration and cleanup
- Apply effects immediately on player join
- Configurable enable/disable per system

### 📝 New Files Created

#### Java Classes (5)
1. `PlaytimeTracker.java` - Tracks and persists player playtime
2. `PersistentEffect.java` - Effect data model with expiration
3. `PersistentEffectManager.java` - Manages persistent effect lifecycle
4. `GrindableRankManager.java` - Handles automatic rank granting
5. `PlayerListener.java` - Event handlers for join/quit

#### Configuration Files (2)
1. `playtime.yml` - Stores player playtime data
2. `effects.yml` - Stores active persistent effects

#### Documentation (3)
1. `FEATURES.md` - Complete feature documentation
2. `QUICKSTART.md` - Quick setup guide
3. `IMPLEMENTATION_SUMMARY.md` - Technical implementation details

### 🔧 Modified Files

#### Core Plugin
- `AxRankMenu.java` - Initialize new systems and event listeners
- `Rank.java` - Added support for `[EFFECT]` action type

#### Configuration
- `config.yml` - Added grindable-ranks and persistent-effects sections
- `ranks.yml` - Added example grindable ranks (VETERAN, LEGEND)
- `lang.yml` - Added new language messages for grindable ranks

#### Build
- `pom.xml` - Updated version to 1.0.0-deii0

### ✨ New Configuration Options

```yaml
# config.yml
grindable-ranks:
  enabled: true
  check-interval: 60

persistent-effects:
  enabled: true
  apply-on-join: true
```

### 🎮 New Action Type

```yaml
# ranks.yml - buy-actions
[EFFECT] <effect_type> <amplifier> <duration_days>

# Examples:
- "[EFFECT] SPEED 1 3"           # Speed II for 3 days
- "[EFFECT] NIGHT_VISION 0 7"    # Night Vision I for 7 days
- "[EFFECT] REGENERATION 0 30"   # Regeneration I for 30 days
```

### 🏆 New Rank Properties

```yaml
# ranks.yml
VETERAN:
  rank: "VETERAN"
  grindable: true                    # NEW: Mark as grindable
  required-playtime-days: 7          # NEW: Days requirement
  # OR
  required-playtime-hours: 168       # NEW: Hours requirement (alternative)
  buy-actions:
    - "[EFFECT] SPEED 1 3"           # NEW: Persistent effect action
```

### 🌍 New Language Keys

```yaml
# lang.yml
grindable-rank:
  received: "&#33FF33Congratulations! You have been granted the &f%rank% &#33FF33rank for your dedication!"
  effect-received: "&#33FF33You have received %effect% (Level %level%) for %days% days!"
```

### 🔄 System Behavior

#### Playtime Tracking
- Starts automatically when player joins
- Updates every 60 seconds
- Auto-saves every 5 minutes
- Final save on player quit
- Data persists in `playtime.yml`

#### Grindable Rank Checks
- Checks all online players every 60 seconds
- Also checks on player join
- Grants rank if eligible
- Prevents duplicate grants via LuckPerms
- Executes all configured buy-actions

#### Persistent Effects
- Applies effects every 30 seconds
- Re-applies on player join
- Checks for expiration every 30 seconds
- Auto-removes expired effects
- Saves data every 5 minutes
- Data persists in `effects.yml`

### 🎯 Use Cases

1. **Loyalty Rewards**: Automatic ranks for playtime milestones
2. **VIP Perks**: Long-duration effects for purchased ranks
3. **Progression System**: Tiered rewards based on dedication
4. **Temporary Boosts**: Effects that expire after X days
5. **Hybrid System**: Combine purchasable and grindable ranks

### 📊 Performance Impact

- **Minimal CPU usage** - Timer-based, not tick-based
- **Low memory footprint** - Efficient data structures
- **Optimized I/O** - Batched saves every 5 minutes
- **Scalable** - Tested with multiple players

### 🔌 Compatibility

- ✅ **LuckPerms** - Required (API v5.4+)
- ✅ **All economy plugins** - Vault, PlayerPoints, etc.
- ✅ **PlaceholderAPI** - Optional
- ✅ **Folia** - Supported
- ✅ **Spigot/Paper** - 1.20.2+
- ✅ **Java 21** - Required

### 🐛 Known Issues

None at this time.

### 📋 Migration Notes

#### From AxRankMenu 1.12.0:
1. All existing configs remain compatible
2. New sections added to config.yml
3. Example ranks added to ranks.yml
4. New data files created automatically
5. No manual migration needed

### 🔮 Future Enhancements (Potential)

- [ ] Web dashboard for playtime stats
- [ ] Leaderboards for most playtime
- [ ] Effect stacking/combining system
- [ ] Custom effect particles/colors
- [ ] Rank reset commands
- [ ] Playtime rewards (items, money)
- [ ] Scheduled effect grants
- [ ] Effect preview system

### 🙏 Credits

- **Base Plugin**: Artillex Studios (AxRankMenu 1.12.0)
- **Custom Features**: deii0
- **Version**: 1.0.0-deii0
- **Release Date**: November 10, 2025

### 📞 Support

For issues or questions about these custom features:
- Review `FEATURES.md` for documentation
- Check `QUICKSTART.md` for setup guide
- Verify config.yml settings
- Check console for errors
- Contact: deii0

---

## Version History

### [1.0.0-deii0] - 2025-11-10
- Initial custom release with grindable ranks and persistent effects

### [1.12.0] - Previous
- Base AxRankMenu version from Artillex Studios

---

**Full Changelog**: https://github.com/deii0/AxRankMenu/compare/1.12.0...1.0.0-deii0
