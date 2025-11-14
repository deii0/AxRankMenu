# Unlimited Items Feature

## Overview
Added support for **unlimited item amounts** in the `[ITEM]` action. You can now give players any amount of items (e.g., 128, 256, 1000+ diamonds), and the system will automatically split them into multiple stacks.

## How It Works

### Before (Limited to 64 per stack)
```yaml
[ITEM] DIAMOND:64:&#FFD700Daily Diamond 7
```
- Could only give up to 64 diamonds (1 stack)
- Amounts over 64 would cause issues

### After (Unlimited Support)
```yaml
[ITEM] DIAMOND:128:&#FFD700Mega Diamond 7      # 2 stacks (64 + 64)
[ITEM] DIAMOND:256:&#FFD700Super Diamond 30     # 4 stacks (64 × 4)
[ITEM] EMERALD:1000:&#00FF00Epic Emerald 90     # 15.625 stacks (15 full + 40 items)
[ITEM] GOLD_INGOT:500:&#FFD700Elite Gold 60     # 7.8125 stacks (7 full + 52 items)
```

## Technical Implementation

The system now:
1. **Calculates** how many full stacks are needed
2. **Splits** the total amount into multiple ItemStacks
3. **Gives** each stack to the player's inventory
4. **Drops** any overflow items if inventory is full
5. **Notifies** player only once if items were dropped

### Code Changes
Modified `PersistentItemRewardManager.giveItem()` method:
- Respects each item's `maxStackSize` (64 for most items, 16 for ender pearls, 1 for tools, etc.)
- Automatically splits large amounts into appropriate stacks
- Handles inventory management intelligently

## Examples

### Example 1: Celestian Rank (128 Diamonds)
```yaml
Celestian:
  buy-actions:
    - '[ITEM] DIAMOND:128:&#FFD700Celestian Diamond 7'
```
**Result:** Player receives 2 stacks of 64 diamonds daily for 7 days

### Example 2: Premium Rank (256 Diamonds)
```yaml
PREMIUM:
  buy-actions:
    - '[ITEM] DIAMOND:256:&#FF00FFPremium Diamond 30'
```
**Result:** Player receives 4 stacks of 64 diamonds daily for 30 days

### Example 3: Elite Rank (500 Gold Ingots)
```yaml
ELITE:
  buy-actions:
    - '[ITEM] GOLD_INGOT:500:&#FFD700Elite Gold 60'
```
**Result:** Player receives 7 full stacks + 52 gold ingots daily for 60 days

### Example 4: Ultimate Rank (1000 Emeralds)
```yaml
ULTIMATE:
  buy-actions:
    - '[ITEM] EMERALD:1000:&#9900FFUltimate Emerald 90'
```
**Result:** Player receives 15 full stacks + 40 emeralds daily for 90 days

## Benefits

✅ **No Limits** - Give any amount of items (1 to infinity)  
✅ **Auto-Splitting** - System handles stack division automatically  
✅ **Smart Inventory** - Fills inventory first, then drops overflow  
✅ **Performance** - Efficient multi-stack handling  
✅ **Backward Compatible** - Existing configs work unchanged  

## Item Stack Sizes Reference

Different items have different max stack sizes:
- **Most items**: 64 (diamonds, emeralds, iron, gold, etc.)
- **Ender Pearls**: 16
- **Snowballs/Eggs**: 16
- **Tools/Weapons**: 1 (can't stack)
- **Armor**: 1 (can't stack)
- **Potions**: 1 (can't stack)

The system automatically respects these limits!

## Use Cases

### 1. High-Tier Rank Rewards
```yaml
- '[ITEM] DIAMOND:512:&#FFD700Ultimate Diamond 7'  # 8 stacks daily
```

### 2. Resource Packs for Events
```yaml
- '[ITEM] IRON_INGOT:1000:&#FFFFFFEvent Iron 1'  # 15.625 stacks once
```

### 3. Economy Boosts
```yaml
- '[ITEM] GOLD_INGOT:320:&#FFD700Gold Boost 30'  # 5 stacks daily for a month
```

### 4. Building Material Gifts
```yaml
- '[ITEM] STONE:6400:&#777777Builder Stone 7'  # 100 stacks!
```

## Updated Documentation

The header in `ranks.yml` now includes:
```yaml
# [ITEM] <material>:<amount>:<name> <duration_in_days>
#   Supports UNLIMITED amounts - items will be split into multiple stacks automatically
#   Example: [ITEM] DIAMOND:128:&#FFD700Mega Diamond 30
#   Gives 128 diamonds per day for 30 days (auto-splits into 2 stacks)
```

## Files Modified

1. **PersistentItemRewardManager.java** - Updated `giveItem()` method with auto-splitting logic
2. **ranks.yml** - Updated documentation and added examples
   - Celestian: 128 diamonds
   - PREMIUM: 256 diamonds
   - ELITE: 500 gold ingots
   - ULTIMATE: 1000 emeralds

---

**Created:** November 13, 2025  
**Feature:** Unlimited Item Support  
**Version:** 1.0.0-deii0 (Custom)
