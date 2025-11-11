# AxRankMenu v1.0.0-deii0 - Complete Changelog

## 🎯 Major Features Added

### 1. **Playtime Tracking System** 
Now reads directly from Minecraft's native statistics system!

**Key Features:**
- ✅ Reads playtime from `world/stats/uuid.json` (Minecraft's built-in stats)
- ✅ Uses `minecraft:play_time` or `minecraft:play_one_minute` statistics
- ✅ Accurate tick-to-milliseconds conversion (20 ticks = 1 second)
- ✅ Automatic caching with 5-minute refresh intervals
- ✅ No custom data storage needed - leverages vanilla Minecraft data

**Technical Details:**
- Parses JSON stats files using Gson
- Converts ticks to human-readable time formats
- Persistent across server restarts (Minecraft handles the storage)
- Performance-optimized with caching

---

### 2. **One-Time Purchase System** 🛒
Prevents players from buying the same rank multiple times!

**Key Features:**
- ✅ Track which ranks each player has purchased
- ✅ Prevents duplicate rank purchases
- ✅ Persistent storage in `purchases.yml`
- ✅ User-friendly error message when attempting to repurchase
- ✅ Admin commands ready for future implementation

**Configuration:**
Add `one-time-purchase: true` to any rank in `ranks.yml`:
```yaml
VIP:
  rank: "VIP"
  price: 1000.0
  one-time-purchase: true  # <-- Add this line
  # ... rest of config
```

**Applied to:**
- ✅ VIP rank
- ✅ Solari rank

---

### 3. **Grindable Ranks System** ⏱️
Automatically grant ranks based on playtime milestones!

**Key Features:**
- ✅ Automatic rank progression based on playtime
- ✅ Integration with LuckPerms for permission management
- ✅ Configurable playtime requirements (days/hours)
- ✅ One-time rewards - players don't get duplicates
- ✅ Checks every 60 seconds for eligible players

**Example Ranks:**
- **VETERAN** - Granted after 7 days (1 week) of playtime
  - Speed II for 3 days
  - Night Vision I for 3 days
  
- **LEGEND** - Granted after 30 days (1 month) of playtime
  - Speed II for 365 days (permanent)
  - Regeneration I for 7 days
  - Strength I for 7 days

**Configuration Example:**
```yaml
VETERAN:
  rank: "VETERAN"
  grindable: true
  required-playtime-days: 7  # or use required-playtime-hours: 168
  buy-actions:
    - "[MESSAGE] Congratulations!"
    - "[CONSOLE] lp user %player% parent set VETERAN"
    - "[EFFECT] SPEED 1 3"  # Speed II for 3 days
```

---

### 4. **Persistent Effects System** 💊
Long-duration potion effects that survive restarts!

**Key Features:**
- ✅ Effects measured in DAYS (not just seconds/minutes)
- ✅ Persist through server restarts
- ✅ Persist through player logouts
- ✅ Automatic expiration system
- ✅ Stored in `effects.yml`
- ✅ Applied every 30 seconds to ensure they're always active
- ✅ Auto-cleanup of expired effects

**New Action Type:**
```yaml
buy-actions:
  - "[EFFECT] SPEED 1 30"  # Speed II for 30 days
  - "[EFFECT] NIGHT_VISION 0 7"  # Night Vision I for 7 days
  - "[EFFECT] REGENERATION 2 365"  # Regeneration III for 1 year
```

**Format:** `[EFFECT] <effect_type> <amplifier> <duration_days>`
- `effect_type`: Any valid PotionEffectType (SPEED, JUMP, REGENERATION, etc.)
- `amplifier`: 0 = Level I, 1 = Level II, 2 = Level III, etc.
- `duration_days`: How many days the effect lasts

---

## 📁 New Files Created

### Java Classes:
1. **`PlaytimeTracker.java`** - Reads playtime from Minecraft stats
2. **`PurchaseTracker.java`** - Tracks and prevents duplicate purchases
3. **`PersistentEffect.java`** - Data model for long-duration effects
4. **`PersistentEffectManager.java`** - Manages persistent effects with expiration
5. **`GrindableRankManager.java`** - Auto-grants ranks based on playtime
6. **`PlayerListener.java`** - Handles join/quit events

### Configuration Files:
1. **`purchases.yml`** - Stores purchase history (auto-generated)
2. **`effects.yml`** - Stores active persistent effects (auto-generated)
3. **`playtime.yml`** - Legacy file (no longer used with stats integration)

---

## 🔧 Modified Files

### Core Files:
- **`AxRankMenu.java`**
  - Initialize PurchaseTracker on startup
  - Initialize PlaytimeTracker, PersistentEffectManager, GrindableRankManager
  - Register PlayerListener for join/quit events
  - Proper shutdown sequence

- **`Rank.java`**
  - Added `[EFFECT]` action support
  - Added one-time purchase validation
  - Mark purchases in PurchaseTracker after successful buy
  - Parse and apply persistent effects

- **`pom.xml`**
  - Version updated to `1.0.0-deii0`
  - Removed BeastTokens dependency (caused build failures)
  - Removed UltraEconomy dependency (caused build failures)

- **`HookManager.java`**
  - Removed BeastTokensHook
  - Removed UltraEconomyHook

### Configuration Files:
- **`config.yml`**
  - Added `grindable-ranks` section with enable toggle
  - Added `persistent-effects` section with enable toggle

- **`ranks.yml`**
  - Added `one-time-purchase: true` to VIP rank
  - Added `one-time-purchase: true` to Solari rank
  - Added VETERAN grindable rank example
  - Added LEGEND grindable rank example

- **`lang.yml`**
  - Added `error.already-purchased` message
  - Added `grindable-rank.received` message
  - Added `grindable-rank.effect-received` message

---

## 🎮 Custom Ranks

### Solari Rank (User Custom)
```yaml
Solari:
  rank: Solari
  price: 2200000
  currency: Vault
  one-time-purchase: true
  buy-actions:
    - "[MESSAGE] &#00EE00You have purchased the &f%name%&#00EE00!"
    - "[CONSOLE] lp user %player% parent set Solari"
    - "[CONSOLE] eco give %player% 1100"
    - "[CONSOLE] crates key give %player% tools_key 15"
    - "[CONSOLE] crates key give %player% armory_key 15"
    - "[CONSOLE] crates key give %player% weapons_key 15"
    - "[CONSOLE] trail endrod %player%"
    - "[CLOSE] menu"
```

**Rewards:**
- Solari LuckPerms rank
- $1,100 refund
- 15x Tools Crate Keys
- 15x Armory Crate Keys
- 15x Weapons Crate Keys
- Endrod particle trail

---

## 🐛 Bug Fixes

### Fixed "File does not exist" Error
**Issue:** Plugin crashed on startup with:
```
java.lang.IllegalStateException: File does not exist
    at PlaytimeTracker.init()
    at PersistentEffectManager.init()
```

**Solution:** Changed Config constructor to use `null` instead of `getResource()` for data files that should be created at runtime.

---

## 🚀 How to Use

### For Purchasable Ranks (One-Time):
```yaml
RANK_NAME:
  rank: "RANK_NAME"
  price: 1000.0
  currency: Vault
  one-time-purchase: true  # <-- Add this!
  buy-actions:
    - "[CONSOLE] lp user %player% parent set RANK_NAME"
```

### For Grindable Ranks:
```yaml
RANK_NAME:
  rank: "RANK_NAME"
  grindable: true  # <-- Mark as grindable!
  required-playtime-days: 7  # or use required-playtime-hours
  buy-actions:
    - "[CONSOLE] lp user %player% parent set RANK_NAME"
    - "[EFFECT] SPEED 1 3"  # Optional: Grant effects
```

### For Effects:
```yaml
buy-actions:
  - "[EFFECT] <TYPE> <LEVEL-1> <DAYS>"
  # Examples:
  - "[EFFECT] SPEED 1 7"        # Speed II for 7 days
  - "[EFFECT] NIGHT_VISION 0 30" # Night Vision I for 30 days
  - "[EFFECT] REGENERATION 2 365" # Regen III for 1 year
```

---

## 📊 System Requirements

- **Minecraft Version:** 1.21.8 (or compatible Paper/Spigot)
- **Java Version:** 21+
- **Required Plugins:** LuckPerms
- **Optional Plugins:** Vault, PlayerPoints, CoinsEngine, RoyaleEconomy
- **Build Tool:** Maven

---

## 🔄 Migration Notes

### From Previous Versions:
1. **Playtime data will be reset** - Now reads from Minecraft stats instead of custom tracking
2. **Purchase history starts fresh** - Existing ranks won't show as "already purchased" until repurchased
3. **Effects storage changed** - Old effects won't carry over (if any existed)

### Removed Dependencies:
- ❌ BeastTokens - Removed due to missing JAR
- ❌ UltraEconomy - Removed due to API issues

---

## 🎉 Credits

**Original Plugin:** Artillex Studios (AxRankMenu v1.12.0)  
**Custom Version:** v1.0.0-deii0  
**Features by:** dei

---

## 📝 Future Improvements

Potential features for future versions:
- Admin commands to manage purchase history
- Web dashboard for playtime statistics
- Discord integration for rank notifications
- Custom sound effects for rank upgrades
- Rank progression animations
- Purchase cooldowns (time-based)
- Family/group purchase bonuses

---

## 💡 Support

If you encounter any issues:
1. Check `purchases.yml` is being created properly
2. Verify `world/stats/uuid.json` exists for players
3. Ensure LuckPerms is installed and working
4. Check console for error messages
5. Verify Gson is available (should be provided by Spigot)

---

**Build Version:** 1.0.0-deii0  
**Build Date:** 2025-11-11  
**JAR Size:** ~3.8 MB  
**Build Status:** ✅ SUCCESS
