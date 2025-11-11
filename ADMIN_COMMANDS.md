# 🛠️ Admin Commands - AxRankMenu v1.0.0-deii0

## Table of Contents
1. [Test Effect Command](#arank-test-effect---test-effect-duration-system)
2. [Test Item Command](#arank-test-item---test-item-reward-system)
3. [Test Playtime Command](#arank-test-playtime---test-playtime-progression)

---

## `/arank test effect` - Test Effect Duration System

This command allows admins to test persistent effects with simulated elapsed time, perfect for testing if effects will expire correctly without waiting days.

**✨ NEW:** You can now exceed the duration days to test expiration! Effects persist through death.

### 📝 Syntax
```
/arank test effect <player> <effect> <duration_days> <simulated_days>
```

### 📋 Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | String | Target player name (must be online) |
| `effect` | String | Potion effect type (e.g., SPEED, REGENERATION) |
| `duration_days` | Integer | Total duration in days (must be positive) |
| `simulated_days` | Integer | Days to simulate as already elapsed (0 or more, **can exceed duration**) |

### 🎯 How It Works

The command adds an effect that appears to have been active for `simulated_days`, allowing you to test:
- ✅ Effect expiration timers
- ✅ Remaining day calculations  
- ✅ Auto-removal of expired effects
- ✅ Storage and persistence
- ✅ **NEW:** Test expired effects (when simulated_days > duration_days)
- ✅ **NEW:** Effects persist through player death and respawn

**Example:** To test if a 7-day effect properly expires:
```
/arank test effect PlayerName SPEED 7 8
```
This creates a SPEED effect that expired 1 day ago (7 days duration, 8 days elapsed).

### 💡 Usage Examples

#### Example 1: Test Fresh Effect
```
/arank test effect dei SPEED 30 0
```
- Gives dei Speed I for 30 days
- No time elapsed (fresh effect)
- Remaining: 30 days

#### Example 2: Test Mid-Duration Effect
```
/arank test effect dei REGENERATION 14 7
```
- Gives dei Regeneration I for 14 days total
- Simulates 7 days already passed
- Remaining: 7 days

#### Example 3: Test Almost Expired Effect
```
/arank test effect dei NIGHT_VISION 7 6
```
- Gives dei Night Vision I for 7 days total
- Simulates 6 days already passed
- Remaining: 1 day

#### Example 4: Test Expired Effect
```
/arank test effect dei HASTE 10 10
```
- Gives dei Haste I for 10 days total
- Simulates 10 days already passed
- Remaining: 0 days (will be removed on next check)

---

## `/arank test item` - Test Item Reward System

This command allows admins to test daily item rewards with simulated elapsed time, perfect for testing the item reward expiration system without waiting days.

### 📝 Syntax
```
/arank test item <player> <item_string> <duration_days> <simulated_days>
```

### 📋 Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | String | Target player name (must be online) |
| `item_string` | String | Item format: `MATERIAL:AMOUNT:NAME:ENCHANTS` |
| `duration_days` | Integer | Total duration in days (must be positive) |
| `simulated_days` | Integer | Days to simulate as already elapsed (0 or more, **can exceed duration**) |

### 🎨 Item String Format

```
MATERIAL:AMOUNT:NAME:ENCHANT:LEVEL,ENCHANT:LEVEL
```

**Examples:**
- `DIAMOND:5:none` - 5 diamonds, no custom name
- `DIAMOND_SWORD:1:&#FFD700Legendary_Sword:SHARPNESS:5,FIRE_ASPECT:2` - Enchanted sword with custom name
- `GOLDEN_APPLE:3:&#FF9900Golden_Cure` - 3 golden apples with name
- `NETHERITE_INGOT:10:&#9966FFMagical_Ingot` - 10 netherite with colored name

### 🎯 How It Works

The command adds a daily item reward that appears to have been active for `simulated_days`:
- ✅ Player receives item immediately (first day)
- ✅ Item given daily at login or hourly check
- ✅ Test expiration by exceeding duration
- ✅ Auto-removal of expired rewards
- ✅ 24-hour cooldown between claims

### 💡 Usage Examples

#### Example 1: Test Fresh Daily Diamonds (7 days)
```
/arank test item dei DIAMOND:5:&#00FFFFDaily_Diamond 7 0
```
- Gives 5 diamonds immediately
- Will give 5 diamonds daily for 7 days
- No time elapsed (fresh reward)
- Remaining: 7 days

#### Example 2: Test Mid-Duration Item Reward
```
/arank test item dei EMERALD:10:&#33FF33Emerald_Reward 14 7
```
- Gives 10 emeralds immediately
- Total duration: 14 days
- Simulates 7 days already passed
- Remaining: 7 days

#### Example 3: Test Expired Item Reward
```
/arank test item dei GOLD_INGOT:8:&#FFD700Gold 5 6
```
- Gives 8 gold ingots immediately
- Total duration: 5 days
- Simulates 6 days passed
- Remaining: **-1 days (EXPIRED!)**
- ⚠️ Will be auto-removed on next cleanup

#### Example 4: Test Enchanted Items
```
/arank test item dei DIAMOND_SWORD:1:&#FFD700Legendary_Blade:SHARPNESS:5,FIRE_ASPECT:2,UNBREAKING:3 30 0
```
- Gives enchanted diamond sword immediately
- Daily rewards for 30 days
- Sword has Sharpness 5, Fire Aspect 2, Unbreaking 3

### ⚙️ Testing Workflow

```
Day 0:  /arank test item dei DIAMOND:5:Test 7 0    → 7 days remaining
Day 3:  Check reward status                        → Should have 4 days remaining
Day 7:  /arank test item dei DIAMOND:5:Test 7 7    → 0 days remaining (expires today)
Day 8:  /arank test item dei DIAMOND:5:Test 7 8    → -1 days (expired, auto-removed)
```

---

## `/arank test playtime` - Test Grindable Ranks System

This command allows admins to simulate playtime for testing grindable rank progression without actually waiting hours/days.

### 📝 Syntax
```
/arank test playtime <player> <hours>
```

### 📋 Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | String | Target player name (must be online) |
| `hours` | Integer | Total playtime to simulate in hours |

### 🎯 How It Works

The command:
1. Sets the player's cached playtime to the specified hours
2. Automatically checks for eligible grindable ranks
3. Grants any ranks the player now qualifies for
4. Executes all rank rewards (effects, items, commands)

### 💡 Usage Examples

#### Example 1: Test ENCHANTER Rank (96 hours)
```
/arank test playtime dei 96
```
- Sets playtime to 96 hours (4 days)
- Should grant ENCHANTER rank
- Rewards: Speed I, Night Vision I, Daily Iron

#### Example 2: Test ARCANIST Rank (336 hours)
```
/arank test playtime dei 336
```
- Sets playtime to 336 hours (14 days / 2 weeks)
- Should grant ARCANIST rank
- Rewards: Speed II, Night Vision I, Haste I, Daily Gold

#### Example 3: Test MAGIUS Rank (730 hours)
```
/arank test playtime dei 730
```
- Sets playtime to 730 hours (~30 days / 1 month)
- Should grant MAGIUS rank
- Rewards: Multiple effects + Daily Diamonds + Daily Books

#### Example 4: Test SOLARI Rank (1800 hours)
```
/arank test playtime dei 1800
```
- Sets playtime to 1800 hours (~75 days / 2.5 months)
- Should grant SOLARI rank
- Rewards: Enhanced effects + Daily Emeralds + Daily Books

#### Example 5: Test CELESTIAN Rank (2920 hours)
```
/arank test playtime dei 2920
```
- Sets playtime to 2920 hours (~121 days / 4 months)
- Should grant CELESTIAN rank (Ultimate!)
- Rewards: Permanent effects + Daily Netherite + Daily Books

### 📊 Command Output

```
✓ Simulated playtime set for PlayerName!
✓ Playtime: 730 hours (30.4 days)
✓ Checking for eligible grindable ranks...
✓ Grindable rank check completed! Check player's permissions.
```

### 🎮 Grindable Ranks Reference

| Rank | Hours Required | Days Equivalent |
|------|---------------|-----------------|
| ENCHANTER | 96 | 4 days |
| ARCANIST | 336 | 14 days (2 weeks) |
| MAGIUS | 730 | 30 days (1 month) |
| SOLARI | 1800 | 75 days (2.5 months) |
| CELESTIAN | 2920 | 121 days (4 months) |

### 🧪 Testing Workflow

**Step 1: Test Below Threshold**
```
/arank test playtime PlayerName 50
```
No ranks should be granted (below 96 hours).

**Step 2: Test ENCHANTER**
```
/arank test playtime PlayerName 96
```
ENCHANTER rank should be granted.

**Step 3: Test ARCANIST**
```
/arank test playtime PlayerName 336
```
ARCANIST rank should be granted (replaces ENCHANTER).

**Step 4: Test Progressive Grants**
```
/arank test playtime PlayerName 100
```
Then:
```
/arank test playtime PlayerName 400
```
Should skip ranks already granted.

**Step 5: Test Ultimate Rank**
```
/arank test playtime PlayerName 3000
```
CELESTIAN rank should be granted with premium rewards!

### ⚠️ Error Messages

**Player not found:**
```
✗ Player not found: PlayerName
```
Solution: Make sure the player is online.

**Invalid hours:**
```
✗ Hours must be a positive number!
```
Solution: Use positive integers (1, 2, 96, 336, etc.).

**Grindable ranks disabled:**
```
⚠ Note: Grindable ranks are disabled in config.yml
```
Solution: Enable in config.yml:
```yaml
grindable-ranks:
  enabled: true
```

### 📝 Notes

- Playtime is cached until next server restart or manual reset
- Real playtime from stats files will override test playtime on next cache update (5 minutes)
- For persistent testing, you may need to repeat the command
- Check LuckPerms to verify rank was granted: `/lp user <player> info`
- Check effects.yml and item-rewards.yml for granted rewards

### 💡 Pro Tips

1. **Quick Testing**: Set exact threshold hours to test rank boundaries
   ```
   /arank test playtime PlayerName 96    # Exactly at ENCHANTER threshold
   /arank test playtime PlayerName 336   # Exactly at ARCANIST threshold
   ```

2. **Testing Rank Skipping**: Set very high hours to see if system grants multiple ranks
   ```
   /arank test playtime PlayerName 5000  # Should grant highest rank only
   ```

3. **Testing Between Thresholds**: Test values between ranks to ensure no false grants
   ```
   /arank test playtime PlayerName 200   # Between ENCHANTER and ARCANIST
   ```

4. **Check All Systems**: After granting, verify:
   - LuckPerms group: `/lp user <player> info`
   - Active effects: Check `effects.yml`
   - Daily rewards: Check `item-rewards.yml`
   - Player receives items in inventory

---

## 📊 Command Output

When you run the command, you'll see:
```
✓ Test effect added to PlayerName!
✓ Effect: SPEED I
✓ Total Duration: 30 days
✓ Simulated: 15 days elapsed
✓ Remaining: 15 days
```

If the effect is expired:
```
⚠ Note: Effect has expired and will be removed on next check!
```

### 🎮 Available Effects

Common effects you can test:
- `SPEED` - Increased movement speed
- `SLOW` - Decreased movement speed
- `HASTE` / `FAST_DIGGING` - Faster mining
- `MINING_FATIGUE` - Slower mining
- `STRENGTH` / `INCREASE_DAMAGE` - Increased melee damage
- `INSTANT_HEALTH` - Instant healing
- `INSTANT_DAMAGE` - Instant damage
- `JUMP_BOOST` - Higher jumping
- `NAUSEA` / `CONFUSION` - Nausea effect
- `REGENERATION` - Health regeneration
- `RESISTANCE` / `DAMAGE_RESISTANCE` - Damage reduction
- `FIRE_RESISTANCE` - Fire immunity
- `WATER_BREATHING` - Underwater breathing
- `INVISIBILITY` - Invisibility
- `BLINDNESS` - Blindness
- `NIGHT_VISION` - Night vision
- `HUNGER` - Increased hunger
- `WEAKNESS` - Decreased melee damage
- `POISON` - Poison damage
- `WITHER` - Wither effect
- `HEALTH_BOOST` - Increased max health
- `ABSORPTION` - Absorption hearts
- `SATURATION` - Instant hunger restore
- `GLOWING` - Glowing outline
- `LEVITATION` - Levitation
- `LUCK` - Increased luck
- `UNLUCK` / `BAD_LUCK` - Decreased luck
- `SLOW_FALLING` - Slow falling
- `CONDUIT_POWER` - Conduit power
- `DOLPHINS_GRACE` - Dolphin's grace
- `BAD_OMEN` - Bad omen
- `HERO_OF_THE_VILLAGE` - Hero effect
- `DARKNESS` - Darkness effect

### ⚠️ Error Messages

**Player not found:**
```
✗ Player not found: PlayerName
```
Solution: Make sure the player is online.

**Invalid effect type:**
```
✗ Invalid effect type: INVALID_NAME
Available effects: SPEED, SLOW, HASTE, ...
```
Solution: Use a valid effect name from the list above.

**Invalid numbers:**
```
✗ Duration and simulate days must be positive numbers!
```
Solution: Use positive integers (1, 2, 3, etc.).

**Simulated days too high:**
```
✗ Simulated days cannot be greater than duration days!
```
Solution: Simulated days must be ≤ duration days.

### 🔒 Permission
```yaml
axrankmenu.admin.test
```
- Default: OP only
- Required to use `/arank test effect` command

### 🧪 Testing Workflow

**Step 1: Test Fresh Effect**
```
/arank test effect PlayerName SPEED 7 0
```
Check: Player should have Speed I effect immediately.

**Step 2: Test Mid-Duration**
```
/arank test effect PlayerName SPEED 7 3
```
Check: Remaining days should show ~4 days.

**Step 3: Test Almost Expired**
```
/arank test effect PlayerName SPEED 7 6
```
Check: Remaining days should show ~1 day.

**Step 4: Test Expired**
```
/arank test effect PlayerName SPEED 7 7
```
Check: Effect should be marked for removal.

**Step 5: Verify Persistence**
- Player logs out
- Wait a moment
- Player logs back in
- Effect should still be active (if not expired)

**Step 6: Verify Auto-Cleanup**
- Wait 30 seconds (effect check interval)
- Expired effects should be automatically removed

### 📝 Notes

- Effects are applied with **Level I** (amplifier 0)
- To test higher levels, modify the command code or use multiple effects
- Effects persist through server restarts
- Effects are stored in `plugins/AxRankMenu/effects.yml`
- Auto-check runs every 30 seconds
- Effects are applied every 30 seconds while player is online

### 🎯 Use Cases

1. **Development Testing**: Quickly test effect expiration without waiting
2. **Bug Reproduction**: Simulate specific scenarios for debugging
3. **Demo Preparation**: Set up effects at specific stages for demonstrations
4. **QA Testing**: Verify effect system works correctly at all stages

---

## 🚫 Update Notifier Disabled

The update notification system has been disabled:
```
❌ AxRankMenu » There is a new version available...
```

This message will no longer appear in:
- Console on startup
- Player join messages

The check can be re-enabled by uncommenting the code in `AxRankMenu.java` if needed.

---

## 📦 Other Admin Commands

### `/arank` or `/arank open`
Opens the ranks GUI for yourself.
- Permission: `axrankmenu.use`
- Default: All players

### `/arank reload`
Reloads all configuration files.
- Permission: `axrankmenu.reload`
- Default: OP only

### `/arank addrank <group>`
Adds a new rank to the GUI from an existing LuckPerms group.
- Permission: `axrankmenu.addrank`
- Default: OP only
- Auto-completes available LuckPerms groups

---

**Version:** 1.0.0-deii0  
**Build:** November 11, 2025  
**Status:** ✅ Fully Functional
