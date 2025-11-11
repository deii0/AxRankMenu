# AxRankMenu v1.0.0-deii0 - Quick Start Guide

## 🚀 Quick Setup (5 Minutes)

### Step 1: Install the Plugin
1. Place `AxRankMenu-1.0.0-deii0.jar` in your `plugins/` folder
2. Ensure LuckPerms is installed (required)
3. Start/restart your server

### Step 2: Enable New Features
Edit `plugins/AxRankMenu/config.yml`:
```yaml
grindable-ranks:
  enabled: true

persistent-effects:
  enabled: true
  apply-on-join: true
```

### Step 3: Configure Your First Grindable Rank
Edit `plugins/AxRankMenu/ranks.yml`:

```yaml
VETERAN:
  rank: "VETERAN"
  server: ""
  grindable: true
  required-playtime-days: 1  # Start with 1 day for testing
  slot: 10
  item:
    type: "DIAMOND"
    name: "&#FFD700&lVETERAN &fRANK"
    lore:
      - " "
      - " &7- &fRequired: &#FFD7001 day playtime"
      - " "
      - "&#FFD700ʀᴇᴡᴀʀᴅs"
      - " &7- Speed II for 3 days"
      - " "
      - "&#FFD700&l(!) &#FFD700Auto-granted!"
  buy-actions:
    - "[MESSAGE] &#FFD700Congratulations! You received VETERAN rank!"
    - "[CONSOLE] lp user %player% parent set VETERAN"
    - "[EFFECT] SPEED 1 3"
```

### Step 4: Reload and Test
```
/rankmenu reload
```

### Step 5: Check Your Progress
Watch the console for:
- `[AxRankMenu] PlaytimeTracker initialized!`
- `[AxRankMenu] PersistentEffectManager initialized!`
- `[AxRankMenu] GrindableRankManager initialized!`

---

## 🎮 Player Experience

### For Grindable Ranks:
1. Player joins server → Playtime tracking starts automatically
2. Player plays for required time (e.g., 7 days)
3. System checks every minute
4. When eligible → Rank automatically granted
5. Player receives configured message
6. Effects applied immediately
7. LuckPerms rank set

### For Effects:
1. Effect granted via `[EFFECT]` action
2. Effect appears on player instantly
3. Effect shows as "permanent" (∞ symbol)
4. Effect persists through logout/login
5. Effect persists through server restart
6. Effect expires after configured days
7. Expired effects removed automatically

---

## 💡 Common Use Cases

### Case 1: Loyalty Reward (Automatic)
**Goal**: Reward players for 1 week of playtime

```yaml
WEEK_VETERAN:
  rank: "WeekVeteran"
  grindable: true
  required-playtime-days: 7
  buy-actions:
    - "[MESSAGE] &#00FF00Thank you for 1 week of playtime!"
    - "[CONSOLE] lp user %player% parent set WeekVeteran"
    - "[EFFECT] SPEED 1 7"  # Speed II for 1 week
```

### Case 2: VIP Purchase (Manual)
**Goal**: Sell VIP rank with 30-day Speed boost

```yaml
VIP:
  rank: "VIP"
  price: 5000
  currency: Vault
  grindable: false
  buy-actions:
    - "[MESSAGE] &#00FF00You purchased VIP rank!"
    - "[CONSOLE] lp user %player% parent set VIP"
    - "[EFFECT] SPEED 0 30"  # Speed I for 30 days
    - "[CLOSE] menu"
```

### Case 3: Long-Term Rank (90 Days)
**Goal**: Hardcore player rank with permanent-like effects

```yaml
LEGEND:
  rank: "Legend"
  grindable: true
  required-playtime-days: 90
  buy-actions:
    - "[MESSAGE] &#FF00FFYou are now a LEGEND!"
    - "[CONSOLE] lp user %player% parent set Legend"
    - "[EFFECT] SPEED 1 365"  # Speed II for 1 year
    - "[EFFECT] NIGHT_VISION 0 365"  # Night Vision for 1 year
    - "[EFFECT] REGENERATION 0 365"  # Regeneration for 1 year
```

---

## 🔍 Troubleshooting

### "Rank not being granted"
✅ Check: Is `grindable: true` set?
✅ Check: Does LuckPerms group exist?
✅ Check: Is player online when threshold is met?
✅ Check: Console for errors
✅ Check: `grindable-ranks.enabled: true` in config.yml

### "Effects not applying"
✅ Check: Is effect name valid? (e.g., SPEED not Speed)
✅ Check: Is format correct? `[EFFECT] TYPE AMPLIFIER DAYS`
✅ Check: Is `persistent-effects.enabled: true`?
✅ Check: Console for errors
✅ Check: Player logged in after grant?

### "Playtime not tracking"
✅ Check: Is player fully logged in?
✅ Check: Any console errors?
✅ Check: Is `playtime.yml` being created?
✅ Check: Server restart to reload?

### "Effects disappear on restart"
✅ This shouldn't happen! Check:
- Is `persistent-effects.enabled: true`?
- Is `apply-on-join: true`?
- Any errors in console on startup?
- Is `effects.yml` being saved?

---

## 📊 Monitoring Your Server

### Check Playtime Data
Look in `plugins/AxRankMenu/playtime.yml`:
```yaml
550e8400-e29b-41d4-a716-446655440000: 604800000  # 7 days in ms
```

### Check Active Effects
Look in `plugins/AxRankMenu/effects.yml`:
```yaml
550e8400-e29b-41d4-a716-446655440000:
  effect_0:
    type: SPEED
    amplifier: 1
    expiration: 1731350400000
```

### Console Messages
Watch for:
- `[AxRankMenu] Player <name> received grindable rank: <rank>`
- `[AxRankMenu] Added effect <type> (Level X) to <uuid> for X days`
- `[AxRankMenu] Loaded X players with active effects!`

---

## 🎯 Pro Tips

### Tip 1: Testing Grindable Ranks
Use `required-playtime-hours: 1` (1 hour) instead of days for testing

### Tip 2: "Permanent" Effects
Use 365 days (1 year) for essentially permanent effects:
```yaml
- "[EFFECT] SPEED 1 365"
```

### Tip 3: Stacking Effects
You can grant multiple effects at once:
```yaml
buy-actions:
  - "[EFFECT] SPEED 1 30"
  - "[EFFECT] NIGHT_VISION 0 30"
  - "[EFFECT] REGENERATION 0 30"
```

### Tip 4: Different Durations
Mix short and long effects:
```yaml
buy-actions:
  - "[EFFECT] SPEED 1 365"  # Long-term
  - "[EFFECT] REGENERATION 0 7"  # Short-term
```

### Tip 5: Combine with Commands
```yaml
buy-actions:
  - "[MESSAGE] Welcome to VIP!"
  - "[CONSOLE] lp user %player% parent set VIP"
  - "[CONSOLE] give %player% diamond 10"
  - "[EFFECT] SPEED 1 30"
  - "[CLOSE] menu"
```

---

## 📈 Recommended Progression

### Starter Rewards (Days 1-7)
```yaml
NEWBIE: required-playtime-days: 1
ACTIVE: required-playtime-days: 3
VETERAN: required-playtime-days: 7
```

### Mid-Tier Rewards (Weeks 2-4)
```yaml
DEDICATED: required-playtime-days: 14
LOYAL: required-playtime-days: 21
ELITE: required-playtime-days: 30
```

### End-Game Rewards (Months+)
```yaml
LEGEND: required-playtime-days: 60
MYTHIC: required-playtime-days: 90
IMMORTAL: required-playtime-days: 180
```

---

## 🔒 Important Notes

1. **Playtime is total, not consecutive** - If a player plays 1 hour per day for 7 days, they get 7 hours total, not 7 days.

2. **Effects persist but aren't "permanent"** - They have expiration dates measured in days.

3. **Grindable ranks check online players only** - Offline players get checked when they log in.

4. **LuckPerms is required** - The rank system uses LuckPerms API.

5. **Backups recommended** - Always backup `playtime.yml` and `effects.yml`.

---

## 🆘 Need Help?

1. Check `FEATURES.md` for detailed documentation
2. Check `IMPLEMENTATION_SUMMARY.md` for technical details
3. Review example configs in `ranks.yml`
4. Check console for error messages
5. Verify all config files have correct syntax

---

**Version**: v1.0.0-deii0  
**Author**: deii0  
**Date**: November 10, 2025

Happy ranking! 🎉
