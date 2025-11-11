# Playtime-Gated Progressive Ranks System

## Overview

The grindable ranks system features a **progressive unlock-and-purchase model**. Ranks are unlocked based on playtime requirements, but players must also **purchase each rank in order** before progressing to the next tier.

## How It Works

### Progressive Requirements

Each rank has **two requirements** that must be met:

1. **Prerequisite Rank** - Must purchase the previous rank first
2. **Playtime Hours** - Must have accumulated enough playtime

### Purchase Flow Example

**Scenario:** Player wants to buy MAGIUS (1 month rank)

```
❌ Step 1: Check if ARCANIST purchased
   → If NO: Show error "You must purchase the ARCANIST rank first!"
   → If YES: Continue to Step 2

✅ Step 2: Check if 730 hours playtime reached
   → If NO: Show error "This rank requires 730 hours..."
   → If YES: Continue to Step 3

✅ Step 3: Check if player has $1,200,000
   → If NO: Show error "You don't have enough money!"
   → If YES: Purchase successful!
```

## Rank Progression Chain

```
ENCHANTER (4 days, $150k)
    ↓ requires ENCHANTER
ARCANIST (2 weeks, $500k)
    ↓ requires ARCANIST
MAGIUS (1 month, $1.2M)
    ↓ requires MAGIUS
SOLARI_GRIND (2.5 months, $2.5M)
    ↓ requires SOLARI_GRIND
CELESTIAN (4 months, $5M)
```

**You CANNOT skip ranks!** Even with 2920 hours playtime, you must buy each rank in order.

## How It Works

## How It Works

### Progressive Requirements

Each rank has **two requirements** that must be met:

1. **Prerequisite Rank** - Must purchase the previous rank first
2. **Playtime Hours** - Must have accumulated enough playtime

### Purchase Flow Example

**Scenario:** Player wants to buy MAGIUS (1 month rank)

```
❌ Step 1: Check if ARCANIST purchased
   → If NO: Show error "You must purchase the ARCANIST rank first!"
   → If YES: Continue to Step 2

✅ Step 2: Check if 730 hours playtime reached
   → If NO: Show error "This rank requires 730 hours..."
   → If YES: Continue to Step 3

✅ Step 3: Check if player has $1,200,000
   → If NO: Show error "You don't have enough money!"
   → If YES: Purchase successful!
```

## Rank Progression Chain

```
ENCHANTER (4 days, $150k)
    ↓ requires ENCHANTER
ARCANIST (2 weeks, $500k)
    ↓ requires ARCANIST
MAGIUS (1 month, $1.2M)
    ↓ requires MAGIUS
SOLARI_GRIND (2.5 months, $2.5M)
    ↓ requires SOLARI_GRIND
CELESTIAN (4 months, $5M)
```

**You CANNOT skip ranks!** Even with 2920 hours playtime, you must buy each rank in order.

### Old System (Removed)
- ❌ Ranks were **automatically granted** when playtime requirements were met
- ❌ Players had no choice in the matter
- ❌ No cost to receive the rank
- ❌ No progression requirement

### New System (Active)
- ✅ Ranks are **unlocked** when playtime requirements are met
- ✅ Players must **purchase** each rank in order
- ✅ Combines playtime dedication with in-game economy
- ✅ **Progressive system** - can't skip ranks

## Rank Configuration

Each grindable rank now requires these fields:

```yaml
ARCANIST:
  rank: "ARCANIST"                     # LuckPerms group name
  server: ""                           # Server context (empty = all servers)
  requires-rank: "ENCHANTER"           # MUST purchase this rank first!
  required-playtime-hours: 336         # Required hours to UNLOCK the rank
  price: 500000                        # Cost to PURCHASE after unlocking
  currency: Vault                      # Currency type (Vault, PlayerPoints, etc.)
  slot: 11                             # GUI slot position
  item:
    # ... item display configuration ...
  buy-actions:
    # ... actions executed on purchase ...
```

**New Field:** `requires-rank` - Prevents purchasing this rank until the specified rank is purchased first.

## Current Grindable Ranks

| Rank | Requires | Playtime Required | Price | Rewards |
|------|----------|------------------|-------|---------|
| **ENCHANTER** | *(none)* | 96 hours (4 days) | $150,000 | Speed I, Night Vision I, Daily Iron x16 (7 days) |
| **ARCANIST** | **ENCHANTER** | 336 hours (2 weeks) | $500,000 | Speed II, Night Vision I, Haste I, Daily Gold x16 (14 days) |
| **MAGIUS** | **ARCANIST** | 730 hours (1 month) | $1,200,000 | Speed II, Night Vision I, Haste I, Regen I, Daily Diamond x8 + Book (30 days) |
| **SOLARI_GRIND** | **MAGIUS** | 1800 hours (2.5 months) | $2,500,000 | Speed II, Night Vision I, Haste II, Regen I, Strength I, Daily Emerald x10 + Books x2 (60 days) |
| **CELESTIAN** | **SOLARI_GRIND** | 2920 hours (4 months) | $5,000,000 | Speed II/Haste II/Night Vision (365 days), Regen II/Strength I/Resistance I (120 days), Daily Netherite x2 + Books x3 |

## Player Experience

### 1. **Prerequisite Not Met** (Previous Rank Not Purchased)
- Player tries to buy MAGIUS without owning ARCANIST
- Error message: `"You must purchase the ARCANIST rank first before unlocking this one!"`
- **Cannot proceed** until previous rank is purchased

### 2. **Locked State** (Insufficient Playtime)
- Prerequisite rank is purchased BUT playtime requirement not met
- Rank appears in `/ranks` GUI but is not purchasable
- Clicking shows error message with current vs required playtime
- Example: `"This rank requires 730 hours of playtime! You currently have 450 hours. (280 hours remaining)"`

### 3. **Unlocked State** (All Requirements Met, Not Purchased)
- Previous rank purchased ✅
- Playtime requirement met ✅
- Rank is now **available for purchase** in the GUI
- Player must have both:
  - ✅ Previous rank owned
  - ✅ Required playtime hours
  - ✅ Sufficient money/currency
- Can click to purchase

### 4. **Purchased State**
- Rank has been bought and all rewards granted
- Player receives:
  - LuckPerms group assignment
  - Persistent potion effects
  - Daily item rewards
  - Any other configured actions

## Error Messages

### Prerequisite Rank Not Purchased
```yaml
error:
  requires-previous-rank: "&#FF3333You must purchase the &#FFAA00%rank% &#FF3333rank first before unlocking this one!"
```

**Placeholder:**
- `%rank%` - Name of the required prerequisite rank

### Insufficient Playtime
```yaml
error:
  insufficient-playtime: "&#FF3333This rank requires %required% hours of playtime! You currently have %current% hours. (%remaining% hours remaining)"
```

**Placeholders:**
- `%required%` - Required hours for this rank
- `%current%` - Player's current playtime hours
- `%remaining%` - Hours remaining until unlock

### Insufficient Currency
```yaml
buy:
  no-currency: "&#FF3333You don't have enough money to buy this rank!"
```

## Implementation Details

### Code Changes

1. **Rank.java** - Added playtime check before purchase:
```java
// Check playtime requirement first
int requiredPlaytimeHours = section.getInt("required-playtime-hours", 0);
if (requiredPlaytimeHours > 0) {
    long currentPlaytimeHours = PlaytimeTracker.getPlaytimeHours(requester.getUniqueId());
    if (currentPlaytimeHours < requiredPlaytimeHours) {
        MESSAGEUTILS.sendLang(requester, "error.insufficient-playtime", 
            java.util.Map.of(
                "%required%", String.valueOf(requiredPlaytimeHours),
                "%current%", String.valueOf(currentPlaytimeHours),
                "%remaining%", String.valueOf(requiredPlaytimeHours - currentPlaytimeHours)
            )
        );
        return;
    }
}
```

2. **GrindableRankManager** - Completely disabled (no automatic granting)

3. **PlaytimeTracker** - Continues to track playtime from Minecraft's stats system

4. **ranks.yml** - All grindable ranks now include `price` and `currency` fields

### Testing Commands

Use the existing test command to simulate playtime:

```bash
# Set a player's playtime to test rank unlocking
/arank test playtime <player> <hours>

# Examples:
/arank test playtime Notch 96        # Unlock ENCHANTER
/arank test playtime Notch 336       # Unlock ARCANIST
/arank test playtime Notch 730       # Unlock MAGIUS
/arank test playtime Notch 1800      # Unlock SOLARI_GRIND
/arank test playtime Notch 2920      # Unlock CELESTIAN
```

**Note:** This only sets the cached playtime. Players must still purchase ranks through the GUI.

## Configuration Files Updated

### lang.yml
- Added `error.insufficient-playtime` message with placeholders

### ranks.yml
- Removed `grindable: true` flag (no longer needed)
- Added `price` field to all grindable ranks
- Added `currency` field to all grindable ranks
- Updated lore to show both playtime requirement and price
- Changed messages from "Automatically granted" to "Unlocks at X hours playtime"

### AxRankMenu.java
- Commented out GrindableRankManager initialization
- System now only uses regular rank purchase flow with playtime checks

## Advantages of New System

1. **Player Agency** - Players choose when to purchase after unlocking
2. **Economy Integration** - Ranks now interact with server economy
3. **Goal-Oriented** - Multi-stage progression (prerequisite → playtime → purchase)
4. **Flexible** - Easy to adjust prices, playtime, or progression order
5. **Rewarding** - Players feel they've earned the right to purchase prestigious ranks
6. **No Skipping** - Forces proper progression through all ranks

## Real-World Example

**Player Journey: Getting CELESTIAN Rank**

```
Day 1-4 (96 hours played):
  ✅ Playtime: 96h reached
  ✅ Prerequisite: None required
  💰 Save $150,000
  → Purchase ENCHANTER rank
  
Week 2-3 (336 hours played):
  ✅ Playtime: 336h reached
  ✅ Prerequisite: ENCHANTER owned ✓
  💰 Save $500,000
  → Purchase ARCANIST rank
  
Month 1 (730 hours played):
  ✅ Playtime: 730h reached
  ✅ Prerequisite: ARCANIST owned ✓
  💰 Save $1,200,000
  → Purchase MAGIUS rank
  
Month 2-3 (1800 hours played):
  ✅ Playtime: 1800h reached
  ✅ Prerequisite: MAGIUS owned ✓
  💰 Save $2,500,000
  → Purchase SOLARI_GRIND rank
  
Month 4+ (2920 hours played):
  ✅ Playtime: 2920h reached
  ✅ Prerequisite: SOLARI_GRIND owned ✓
  💰 Save $5,000,000
  → Purchase CELESTIAN rank (FINAL!)
```

**Total Investment:** 2920+ hours playtime + $9,350,000 in-game currency

## Migration Notes

- **No data migration needed** - Old grindable ranks were granted automatically
- Players who received ranks under the old system keep them
- New players experience the unlock-then-purchase system
- Consider announcing the change to your players

## Future Enhancements (Optional)

Potential additions you could make:

1. **Discount System** - Reduce price based on current rank weight
2. **Playtime Display** - Show `/playtime` command in GUI tooltips
3. **Progress Bar** - Visual indicator of progress toward unlock
4. **Notification** - Alert players when a rank becomes unlocked
5. **GUI Filtering** - Hide locked ranks or show them with different appearance

## Support

If you encounter issues:
1. Check console for errors
2. Verify LuckPerms groups exist
3. Test with `/arank test playtime` command
4. Check player's actual playtime with Minecraft stats
5. Verify economy plugin (Vault) is working

---

**Version:** AxRankMenu 1.0.0-deii0  
**Build Date:** November 11, 2025  
**System:** Playtime-Gated Purchase System
