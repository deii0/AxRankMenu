# 🎁 [ITEM] Action Feature - Daily Item Rewards

## Overview
The new `[ITEM]` action allows ranks to grant items to players **daily** for a specified duration. This is perfect for VIP perks, subscription-style rewards, or long-term incentives!

---

## 🎯 How It Works

When a player purchases a rank or receives a grindable rank with `[ITEM]` actions:
1. **First claim**: Player receives items immediately
2. **Daily claims**: Player gets the same items every 24 hours
3. **Duration**: Continues for the specified number of days
4. **Auto-delivery**: Items are given on login if a day has passed
5. **Automatic checks**: System checks every hour for eligible claims

---

## 📝 Syntax

```yaml
buy-actions:
  - "[ITEM] <item_format> <duration_days>"
```

### Item Format Options:

#### **Simple Format:**
```yaml
- "[ITEM] MATERIAL:AMOUNT 7"
```
Example: `[ITEM] DIAMOND:5 7` - 5 diamonds daily for 7 days

#### **With Custom Name:**
```yaml
- "[ITEM] MATERIAL:AMOUNT:NAME 30"
```
Example: `[ITEM] EMERALD:10:&#00FFFFVip Emerald 30` - 10 emeralds with custom name, daily for 30 days

#### **With Enchantments:**
```yaml
- "[ITEM] MATERIAL:AMOUNT:NAME:ENCHANT:LEVEL,ENCHANT:LEVEL 30"
```
Example: `[ITEM] DIAMOND_SWORD:1:&#FF0000Legendary Sword:SHARPNESS:5,FIRE_ASPECT:2 30`

**Format Breakdown:**
- `MATERIAL` - Minecraft material type (e.g., DIAMOND, GOLDEN_APPLE)
- `AMOUNT` - Number of items (e.g., 1, 5, 64)
- `NAME` - Custom display name with color codes (use `none` to skip)
- `ENCHANT:LEVEL` - Enchantment and level (e.g., SHARPNESS:5)
- Multiple enchantments separated by commas
- `duration_days` - How many days the reward lasts

---

## 💎 Examples

### Basic Items
```yaml
buy-actions:
  - "[ITEM] DIAMOND:5 7"  # 5 diamonds daily for 1 week
  - "[ITEM] GOLDEN_APPLE:3 30"  # 3 golden apples daily for 1 month
  - "[ITEM] IRON_INGOT:16 14"  # 16 iron daily for 2 weeks
```

### Custom Named Items
```yaml
buy-actions:
  - "[ITEM] EMERALD:10:&#00FFFFVip Emerald 30"
  - "[ITEM] DIAMOND:5:&#FFD700Premium Diamond 7"
  - "[ITEM] ENCHANTED_BOOK:2:&#FF00FFLegend Book 30"
```

### Enchanted Items
```yaml
buy-actions:
  - "[ITEM] DIAMOND_SWORD:1:&#FF0000Legendary Sword:SHARPNESS:5,FIRE_ASPECT:2 30"
  - "[ITEM] DIAMOND_PICKAXE:1:VIP Pickaxe:EFFICIENCY:5,UNBREAKING:3,FORTUNE:3 30"
  - "[ITEM] BOW:1:Elite Bow:POWER:5,INFINITY:1,FLAME:1 14"
```

---

## 🎮 Real Configuration Examples

### VIP Rank
```yaml
VIP:
  rank: "VIP"
  price: 1000.0
  currency: Vault
  one-time-purchase: true
  slot: 12
  item:
    type: "LIME_BANNER"
    name: "&#00FF00&lVIP &fRANK"
    lore:
      - " "
      - " &7- &fPrice: &#00AA00$%price%"
      - " &7- &fDaily Rewards:"
      - "   &a• 5x Diamonds (7 days)"
      - " "
      - "&#00FF00&l(!) &#00FF00Click here to purchase!"
  buy-actions:
    - "[MESSAGE] &#00EE00You have purchased VIP rank!"
    - "[CONSOLE] lp user %player% parent set VIP"
    - "[ITEM] DIAMOND:5:&#00FF00VIP Diamond 7"
    - "[CLOSE] menu"
```

### Solari Rank (Premium)
```yaml
Solari:
  rank: Solari
  price: 2200000
  currency: Vault
  one-time-purchase: true
  slot: 14
  item:
    type: LIGHT_BLUE_BANNER
    name: '&#00FFFF&lSolari &fRANK'
    lore:
      - ' '
      - ' &7- &fPrice: &#00AA00$%price%'
      - ' &7- &fDaily Rewards:'
      - '   &b• 10x Emeralds (30 days)'
      - '   &b• 3x Golden Apples (30 days)'
      - ' '
      - '&#00FFFF&l(!) &#00FFFFClick here to purchase!'
  buy-actions:
    - '[MESSAGE] &#00EE00You have purchased Solari rank!'
    - '[CONSOLE] lp user %player% parent set Solari'
    - '[CONSOLE] eco give %player% 1100'
    - '[CONSOLE] crates key give %player% tools_key 15'
    - '[ITEM] EMERALD:10:&#00FFFFSolari Emerald 30'
    - '[ITEM] GOLDEN_APPLE:3:&#FFD700Solari Golden Apple 30'
    - '[CLOSE] menu'
```

### VETERAN Grindable Rank
```yaml
VETERAN:
  rank: "VETERAN"
  grindable: true
  required-playtime-days: 7
  slot: 10
  item:
    type: "DIAMOND"
    name: "&#FFD700&lVETERAN &fRANK"
    lore:
      - " "
      - " &7- &fRequired: 7 days playtime"
      - " &7- &fDaily Rewards:"
      - "   &6• 16x Iron Ingots (7 days)"
      - " "
      - "&#FFD700&l(!) &#FFD700Automatically granted!"
  buy-actions:
    - "[MESSAGE] &#FFD700Congratulations! VETERAN rank received!"
    - "[CONSOLE] lp user %player% parent set VETERAN"
    - "[EFFECT] SPEED 1 3"
    - "[ITEM] IRON_INGOT:16:&#FFD700Veteran Iron 7"
```

### LEGEND Grindable Rank
```yaml
LEGEND:
  rank: "LEGEND"
  grindable: true
  required-playtime-days: 30
  slot: 16
  item:
    type: "NETHER_STAR"
    name: "&#FF00FF&lLEGEND &fRANK"
    lore:
      - " "
      - " &7- &fRequired: 30 days playtime"
      - " &7- &fDaily Rewards:"
      - "   &d• 2x Enchanted Books (30 days)"
      - "   &d• 10x Diamonds (30 days)"
      - " "
      - "&#FF00FF&l(!) &#FF00FFAutomatically granted!"
  buy-actions:
    - "[MESSAGE] &#FF00FFCongratulations! LEGEND rank received!"
    - "[CONSOLE] lp user %player% parent set LEGEND"
    - "[EFFECT] SPEED 1 365"
    - "[ITEM] ENCHANTED_BOOK:2:&#FF00FFLegend Book 30"
    - "[ITEM] DIAMOND:10:&#FF00FFLegend Diamond 30"
```

---

## 🔧 Technical Details

### Storage
- Items stored in: `plugins/AxRankMenu/item-rewards.yml`
- Tracks: Player UUID, item details, duration, last claim time
- Auto-saves when modified

### Timing System
- **Check Interval**: Every 1 hour (configurable in config.yml)
- **24-Hour Window**: Players can claim once per 24 hours
- **On Login**: Pending daily rewards given immediately on join
- **Expiration**: Automatically removes expired rewards

### Inventory Management
- Items added directly to player inventory
- If inventory full: Items drop at player's feet
- Player notified: "Your inventory is full! Items dropped at your feet."

### Messages
When receiving daily items:
```
§a§lYou have received VIP Diamond x5!
§6§lYou will receive this item daily for the next 7 days!
```

When claiming daily reward:
```
§a§lDaily reward: VIP Diamond x5!
§6§lRemaining days: 6
```

---

## ⚙️ Configuration

### In `config.yml`:
```yaml
persistent-item-rewards:
  enabled: true
  # How often to check and give daily rewards (in hours)
  check-interval: 1
```

---

## 📊 Use Cases

### 1. **VIP Subscription Model**
```yaml
- "[ITEM] DIAMOND:5 7"  # Weekly diamond delivery
- "[ITEM] GOLDEN_APPLE:3 7"  # Weekly golden apples
```

### 2. **Monthly Premium Perks**
```yaml
- "[ITEM] EMERALD:10 30"  # Monthly emerald delivery
- "[ITEM] ENCHANTED_BOOK:2 30"  # Monthly enchanted books
```

### 3. **Trial Period**
```yaml
- "[ITEM] IRON_INGOT:32 3"  # 3-day trial with iron
```

### 4. **Long-Term Investment**
```yaml
- "[ITEM] DIAMOND:1 365"  # Daily diamond for a year
- "[ITEM] NETHERITE_INGOT:1 365"  # Daily netherite for a year
```

### 5. **Starter Pack**
```yaml
- "[ITEM] COOKED_BEEF:64 7"  # Food for the first week
- "[ITEM] TORCH:64 7"  # Torches for the first week
```

---

## 🎯 Best Practices

1. **Balance Duration with Value**
   - Short duration (3-7 days): Higher value items
   - Long duration (30+ days): Moderate value items

2. **Use Custom Names**
   - Makes items feel special
   - Shows which rank they came from
   - Example: `&#00FFFFVip Emerald` instead of plain `Emerald`

3. **Combine with Effects**
   ```yaml
   - "[EFFECT] SPEED 1 30"  # Persistent effect
   - "[ITEM] DIAMOND:5 30"  # Daily items
   ```

4. **Test Values First**
   - Start conservative
   - Monitor economy impact
   - Adjust based on player feedback

5. **Clear Communication**
   - Update rank lore to show daily rewards
   - Inform players about 24-hour cooldown
   - Display remaining days

---

## 🐛 Troubleshooting

**Items not being given?**
- Check `item-rewards.yml` for active rewards
- Verify `persistent-item-rewards.enabled: true` in config
- Ensure player has logged in after 24 hours

**Invalid item format?**
- Check console for error messages
- Verify material name is correct (uppercase)
- Test format: `MATERIAL:AMOUNT:NAME duration`

**Player inventory full?**
- Items automatically drop at player's feet
- Player receives notification
- Items are never lost

---

## 📈 Monitoring

Check active rewards:
```yaml
# In item-rewards.yml
"uuid-here":
  - material: DIAMOND
    amount: 5
    duration-days: 7
    start-time: 1699660800000
    last-claim-time: 1699747200000
    name: "§a§lVIP Diamond"
```

Console messages:
```
§a[AxRankMenu] PersistentItemRewardManager initialized - Loaded 15 players with active rewards!
```

---

## 🚀 Advanced Examples

### Enchanted Diamond Armor Set (Daily for 30 days)
```yaml
- "[ITEM] DIAMOND_HELMET:1:&#00FFFFVip Helmet:PROTECTION:4,UNBREAKING:3 30"
- "[ITEM] DIAMOND_CHESTPLATE:1:&#00FFFFVip Chestplate:PROTECTION:4,UNBREAKING:3 30"
- "[ITEM] DIAMOND_LEGGINGS:1:&#00FFFFVip Leggings:PROTECTION:4,UNBREAKING:3 30"
- "[ITEM] DIAMOND_BOOTS:1:&#00FFFFVip Boots:PROTECTION:4,UNBREAKING:3,FEATHER_FALLING:4 30"
```

### Daily PvP Kit
```yaml
- "[ITEM] DIAMOND_SWORD:1:PvP Sword:SHARPNESS:5,FIRE_ASPECT:2 30"
- "[ITEM] BOW:1:PvP Bow:POWER:5,INFINITY:1 30"
- "[ITEM] ARROW:64:none 30"
- "[ITEM] GOLDEN_APPLE:5:none 30"
```

### Resource Pack
```yaml
- "[ITEM] DIAMOND:10 30"
- "[ITEM] EMERALD:5 30"
- "[ITEM] IRON_INGOT:32 30"
- "[ITEM] GOLD_INGOT:16 30"
```

---

## ✅ Features Summary

✅ **Daily automatic delivery**  
✅ **Configurable duration (days)**  
✅ **Custom item names with colors**  
✅ **Enchantment support**  
✅ **Persistent across restarts**  
✅ **Auto-cleanup of expired rewards**  
✅ **Inventory full protection**  
✅ **Works with grindable ranks**  
✅ **Works with purchased ranks**  
✅ **Hourly automated checks**  

---

**Version:** 1.0.0-deii0  
**Feature:** Daily Item Rewards System  
**Status:** ✅ Fully Functional
