# AxRankMenu v1.0.0-deii0 - Quick Summary

## ✅ Both Features Implemented!

### 1. ✅ Playtime from world/stats
- Reads directly from Minecraft's `world/stats/uuid.json`
- Uses native `minecraft:play_time` statistic
- No custom tracking needed
- Updates every 5 minutes

### 2. ✅ One-Time Purchase System
- Created `PurchaseTracker.java` 
- Stores in `purchases.yml`
- Added `one-time-purchase: true` flag to ranks
- Shows error if player tries to repurchase
- Applied to VIP and Solari ranks

## 🎉 Build Status: SUCCESS

```
[INFO] BUILD SUCCESS
[INFO] Total time:  12.531 s
File: target/AxRankMenu-1.0.0-deii0.jar
Size: ~3.8 MB
```

## 📋 Files Modified

**New Files:**
- `PurchaseTracker.java` - Tracks purchases
- `PlaytimeTracker.java` - Rewritten to use stats files

**Modified Files:**
- `AxRankMenu.java` - Initialize PurchaseTracker
- `Rank.java` - Check purchases before allowing buy
- `ranks.yml` - Added `one-time-purchase: true` to VIP & Solari
- `lang.yml` - Added error message for duplicate purchases

## 🎮 How It Works

**One-Time Purchase:**
```yaml
VIP:
  rank: "VIP"
  one-time-purchase: true  # <-- Prevents repurchasing
  buy-actions:
    - "[CONSOLE] lp user %player% parent set VIP"
```

When player tries to buy again:
> "You have already purchased this rank! This is a one-time purchase only."

**Playtime Tracking:**
- Reads from `world/stats/<uuid>.json`
- Looks for `minecraft:play_time` 
- Converts ticks → time (20 ticks = 1 second)
- Used by grindable ranks system

## 🚀 Ready to Use!

Copy `target/AxRankMenu-1.0.0-deii0.jar` to your server and restart!

See `CHANGELOG_v1.0.0-deii0.md` for complete documentation.


# ===== HOW TO USE SPECIAL ACTIONS =====# ----- GUI SETTINGS -----

# title: "&0&lʀᴀɴᴋs"

# [EFFECT] <effect_name> <amplifier> <duration_in_days># a gui can only have 1-6 rows

#   Example: [EFFECT] SPEED 0 7rows: 3

#   Grants Speed I effect for 7 days (amplifier 0 = level I, 1 = level II, etc.)# valid values: CHEST, WORKBENCH, HOPPER, DISPENSER, BREWING

#   Effect persists through death and server restartstype: CHEST

#

# [ITEM] <material> <amount> <duration_in_days># ----- ITEMS -----

#   Example: [ITEM] DIAMOND 64 7

#   Gives 64 diamonds per day for 7 daysVIP:

#   Player receives items daily until duration expires  # the name of the rank

#  rank: "VIP"

# required-playtime-hours: <hours>  # set to "" to use the current server

#   Example: required-playtime-hours: 24  server: ""

#   Player must have at least this many hours of playtime to purchase the rank  # the price of the rank (check the hooks section)

#   Use 0 for no playtime requirement  price: 1000.0

  # check the hooks section