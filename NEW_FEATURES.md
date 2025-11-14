# New Features Added - Main Menu & Paid Ranks

## Overview
Added a new multi-page rank menu system with support for both in-game purchasable ranks and Discord-only paid ranks.

## Features

### 1. Main Menu Hub (`/rank`)
When players type `/rank`, they now see a main menu with two options:
- **Purchasable Ranks** - In-game ranks buyable with server currency
- **Paid Ranks** - Premium ranks available only through Discord

### 2. Purchasable Ranks GUI
- Displays all ranks that can be purchased with in-game money
- Filtered view showing only ranks without `paid-only: true`
- Includes back button to return to main menu
- Retains all existing functionality (playtime requirements, effects, items, etc.)

### 3. Paid Ranks GUI
- Displays premium ranks available only through Discord
- Shows ranks marked with `paid-only: true`
- When clicked, sends players the Discord link
- Non-purchasable in-game (display only)
- Includes back button to return to main menu

## New Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/rank` | `axrankmenu.use` | Opens the main menu hub |
| `/rank purchasable` | `axrankmenu.use` | Directly opens purchasable ranks |
| `/rank paid` | `axrankmenu.use` | Directly opens paid ranks |

## Configuration

### config.yml

Added new section:
```yaml
# ===== MAIN MENU CONFIGURATION =====
main-menu:
  title: "&8&lRank Menu"
  type: CHEST
  rows: 3
  buttons:
    purchasable-ranks:
      slot: 11
      item:
        type: EMERALD
        name: "&#00FF00&lPurchasable Ranks"
        lore:
          - " "
          - "&7Click to view ranks you can"
          - "&7purchase with in-game money!"
    paid-ranks:
      slot: 15
      item:
        type: DIAMOND
        name: "&#00AAFF&lPaid Ranks"
        lore:
          - " "
          - "&7Premium ranks available"
          - "&7through our Discord server!"

# ===== PAID RANKS CONFIGURATION =====
paid-ranks:
  discord-link: "https://discord.gg/yourserver"
  send-clickable-link: true
```

### ranks.yml

Added new sections:
```yaml
# ===== PURCHASABLE RANKS GUI =====
purchasable-ranks:
  title: "&0&lPurchasable Ranks"
  type: CHEST
  rows: 6
  back-button:
    slot: 0
    item:
      type: ARROW
      name: "&c&lBack"

# ===== PAID RANKS GUI =====
paid-ranks:
  title: "&0&lPaid Ranks (Discord Only)"
  type: CHEST
  rows: 6
  back-button:
    slot: 0
    item:
      type: ARROW
      name: "&c&lBack"
```

### Marking Ranks as Paid-Only

To make a rank Discord-only, add `paid-only: true`:

```yaml
PREMIUM:
  rank: PREMIUM
  server: ''
  slot: 11
  paid-only: true  # Discord only!
  item:
    type: NETHER_STAR
    name: '&#FF00FF&lPREMIUM &fRANK'
    lore:
      - '&#FFD700&lPurchase on Discord!'
```

To keep a rank purchasable in-game, use `paid-only: false` or omit it:

```yaml
VIP:
  rank: VIP
  price: 1000.0
  currency: Vault
  paid-only: false  # Or omit this line entirely
```

### lang.yml

Added new message:
```yaml
paid-ranks:
  purchase-message: "&#FFD700This rank can only be purchased through Discord! &#FFFFFFVisit: &#00AAFF%link%"
```

## New Files Created

1. **MainMenuGui.java** - Main hub menu controller
2. **PurchasableRanksGui.java** - In-game purchasable ranks GUI
3. **PaidRanksGui.java** - Discord-only paid ranks GUI

## Modified Files

1. **Commands.java** - Updated to open main menu by default
2. **GuiUpdater.java** - Added support for refreshing new GUI types
3. **config.yml** - Added main menu and paid ranks configuration
4. **ranks.yml** - Added GUI sections and example paid ranks
5. **lang.yml** - Added paid ranks message

## Example Paid Ranks Included

Three example paid ranks are included:

1. **PREMIUM** - Entry-level Discord rank
2. **ELITE** - Mid-tier Discord rank
3. **ULTIMATE** - Top-tier Discord rank

All include example perks and benefits to showcase the system.

## How It Works

### Player Experience:

1. Player types `/rank`
2. Main menu opens with 2 buttons
3. Click "Purchasable Ranks" → See in-game purchasable ranks
4. Click "Paid Ranks" → See Discord-only ranks
5. Clicking a paid rank → Closes menu and sends Discord link

### Admin Setup:

1. Configure Discord link in `config.yml`
2. Mark ranks as `paid-only: true` in `ranks.yml`
3. Customize GUI layouts and items
4. Players can now view and differentiate between rank types

## Benefits

✅ **Clear Separation** - Players know which ranks are purchasable vs. paid
✅ **Better Organization** - No clutter in a single menu
✅ **Upselling** - Easy to showcase premium Discord ranks
✅ **Flexible** - Can customize each menu independently
✅ **Backward Compatible** - Existing ranks work without changes

## Next Steps

1. **Update Discord link** in `config.yml` → `paid-ranks.discord-link`
2. **Customize main menu** appearance in `config.yml` → `main-menu`
3. **Add/modify ranks** with `paid-only: true/false` flag
4. **Test the menus** with `/rank`, `/rank purchasable`, `/rank paid`
5. **Reload plugin** with `/rank reload`

---

**Created:** November 13, 2025  
**Version:** 1.0.0-deii0 (Custom)
